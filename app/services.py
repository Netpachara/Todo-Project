import functools
import json
import multiprocessing as mp
import traceback
from concurrent.futures import ProcessPoolExecutor
from datetime import datetime
from functools import cmp_to_key

import boto3
import dateutil
import numpy
import pandas as pd
import pymssql
import pytz
from google.cloud import bigquery
from google.oauth2 import service_account

from app.utils import round_half_up, Models, days_between, GbqBookingSummary, compare, GbqBookingDashboardModel, \
    GbqProduct, GbqDestination, GbqSupplier, GbqBookingBooker, GbqCompany, get_df_from_query
from app.models.etl_date_type_request import ETLDateType

POOL_SIZE = mp.cpu_count()


class ETLBookingService:

    def __init__(self, config_client) -> None:
        self._config_client = config_client
        self._aws_access_key_id = config_client.get_attribute('aws.access.key.id')
        self._aws_secret_access_key = config_client.get_attribute('aws.secret.access.key')
        self._region_name = config_client.get_attribute('aws.region')
        self._queue_url_etl_booking = config_client.get_attribute('sqs.queue.url.etl-booking')
        self._queue_url_etl_booking_operation = config_client.get_attribute('sqs.queue.url.etl-booking-operation')
        self._queue_url_etl_booking_validation = config_client.get_attribute('sqs.queue.url.etl-booking-validation')
        google_key = self._config_client.get_attribute('google.key')
        service_account_info = json.loads(google_key, strict=False)
        credentials = service_account.Credentials.from_service_account_info(service_account_info)

        self._credentials = credentials
        self._gbq_group_table = config_client.get_attribute('google.bq.group.table')
        self._gbq_booking_booker_table = config_client.get_attribute('google.bq.table.booking-booker')
        self._gbq_booking_summary_table = config_client.get_attribute('google.bq.table.booking-summary')
        self._gbq_booking_operation_table = config_client.get_attribute('google.bq.table.booking-operation')
        self._gbq_product_table = config_client.get_attribute('google.bq.table.product')
        self._gbq_company_table = config_client.get_attribute('google.bq.table.company')
        self._gbq_supplier_table = config_client.get_attribute('google.bq.table.supplier')
        self._gbq_destination_table = config_client.get_attribute('google.bq.table.destination')
        self._gbq_company_retention_table = config_client.get_attribute('google.bq.table.company-retention')

    def do_df_booking_dashboard(self, df_booking_booker, df_booking_supplier, df_booking_hotel,
                                df_booking_roomtype_condition, df_booking_guest, df_booking_cancel, 
                                df_booking_payment, df_booking_payment_credit_terms, df_booking_credit_terms_checkout_field,
                                date_now, row_tuple):
        gbq_booking_summary_list = []
        gbq_booking_booker_list = []
        error_booking_id = []
        row = row_tuple[1]
        print("[ETL BOOKING DASHBOARD] Booking id => ", row.bookingid)
        try:
            selected_booking_booker = df_booking_booker.loc[df_booking_booker['BookingID'] == row.bookingid].copy()
            selected_booking_booker = selected_booking_booker.reset_index()
            selected_booking_supplier = df_booking_supplier.loc[df_booking_supplier['BookingID'] == row.bookingid].copy()
            selected_booking_supplier = selected_booking_supplier.reset_index()
            selected_booking_hotel = df_booking_hotel.loc[df_booking_hotel['BookingID'] == row.bookingid].copy()
            selected_booking_hotel = selected_booking_hotel.reset_index()
            selected_booking_roomtype_condition = df_booking_roomtype_condition.loc[df_booking_roomtype_condition['BookingSupplierID'] == selected_booking_supplier.BookingSupplierID.values[0]].copy()
            selected_booking_roomtype_condition.reset_index()
            selected_booking_roomtype_condition['TotalCost'] = selected_booking_roomtype_condition['Cost'] * selected_booking_roomtype_condition['Quantity']

            selected_booking_guest = df_booking_guest.loc[df_booking_guest['BookingID'] == row.bookingid].copy()
            selected_booking_guest = selected_booking_guest.reset_index()
            selected_booking_guest_main = selected_booking_guest.loc[selected_booking_guest.index == 0].copy()
            selected_booking_guest_main = selected_booking_guest_main.reset_index()
            selected_booking_other_guest = selected_booking_guest.loc[selected_booking_guest.index > 0].copy()
            selected_booking_other_guest = selected_booking_other_guest.reset_index()

            selected_booking_cancel = df_booking_cancel.loc[df_booking_cancel['BookingID'] == row.bookingid].copy()
            selected_booking_cancel = selected_booking_cancel.reset_index()

            selected_booking_payment_all_booker = df_booking_payment.loc[
                df_booking_payment['BookingBookerID'].isin(selected_booking_booker["BookingBookerID"])]
            selected_booking_payment_all_booker = selected_booking_payment_all_booker.reset_index()

            booker_cost_sum = 0
            booker_cancellation_cost_sum = 0
            for booker_index, selected_booking_booker_row in selected_booking_booker.iterrows():
                selected_booking_payment = df_booking_payment.loc[df_booking_payment['BookingBookerID'] == selected_booking_booker_row.BookingBookerID].copy()
                selected_booking_payment = selected_booking_payment.reset_index()
                selected_credit_terms = selected_booking_payment.loc[selected_booking_payment['PaymentType'] == 'credit_terms'].copy()
                selected_credit_terms = selected_credit_terms.reset_index()
                selected_credit_terms_all_booker = selected_booking_payment_all_booker.loc[selected_booking_payment_all_booker['PaymentType'] == 'credit_terms'].copy()
                selected_credit_terms_all_booker = selected_credit_terms_all_booker.reset_index()
                booker_exchange_rate = selected_booking_payment.BookerExchangeRate.values[0] if pd.notna(selected_booking_payment.BookerExchangeRate.values[0]) else 1

                gbq_booking_booker_tmp = GbqBookingBooker()
                gbq_booking_booker_tmp.BookingID = row.bookingid
                gbq_booking_booker_tmp.BookingDateTime = row.bookingdate
                gbq_booking_booker_tmp.TravelingObjective = row.purpose
                gbq_booking_booker_tmp.CompanyName = selected_booking_booker_row['CompanyNameEN']
                gbq_booking_booker_tmp.BookerEmail = selected_booking_booker_row['BookerEmail']
                gbq_booking_booker_tmp.BookingStatus = Models.switcher_booking_status.get(row.bookingstatus, None)
                gbq_booking_booker_tmp.SupplierName = selected_booking_supplier.SupplierName.values[0]
                gbq_booking_booker_tmp.DestinationID = selected_booking_hotel.DestinationID.values[0]
                gbq_booking_booker_tmp.Destination = selected_booking_hotel.DestinationNameEN.values[0]
                gbq_booking_booker_tmp.ProductName = selected_booking_hotel.HotelNameEN.values[0]
                gbq_booking_booker_tmp.CheckInDay = row.checkin.strftime('%A')
                gbq_booking_booker_tmp.CheckInDate = row.checkin
                gbq_booking_booker_tmp.CheckOutDate = row.checkout
                gbq_booking_booker_tmp.UpdatedDate = date_now
                gbq_booking_booker_tmp.CancelDate = row.canceldate
                gbq_booking_booker_tmp.CancelReason = row.cancelreason
                gbq_booking_booker_tmp.IsMainBooker = selected_booking_booker_row['MainBooker'] == True

                gbq_booking_booker_tmp.BookerName = selected_booking_booker_row['BookerFullName']
                gbq_booking_booker_tmp.TravellerName = selected_booking_guest_main.FullName.values[0]
                gbq_booking_booker_tmp.TravellerEmail = selected_booking_guest_main.Email.values[0]
                gbq_booking_booker_tmp.OtherTravellerEmail = ", ".join(set(selected_booking_other_guest['Email'].tolist()))
                gbq_booking_booker_tmp.OtherTravellerName = ", ".join(set(selected_booking_other_guest['FullName'].tolist()))
                gbq_booking_booker_tmp.BusinessArea = selected_booking_booker_row['BookerBusinessArea']

                approval_status = None
                if not selected_credit_terms.empty:
                    gbq_booking_booker_tmp.SellingPriceCreditTerms = round_half_up(selected_credit_terms.ChargeTotal.values[0] * booker_exchange_rate)
                    selected_booking_payment_credit_terms = df_booking_payment_credit_terms.loc[df_booking_payment_credit_terms['BookingPaymentID'] == selected_credit_terms.BookingPaymentID.values[0]]
                    if not selected_booking_payment_credit_terms.empty:
                        selected_booking_credit_terms_checkout_field = df_booking_credit_terms_checkout_field.loc[
                            df_booking_credit_terms_checkout_field['BookingCreditTermsID'] == selected_booking_payment_credit_terms.BookingCreditTermsID.values[0]].copy()
                        selected_booking_credit_terms_checkout_field = selected_booking_credit_terms_checkout_field.reset_index()
                        if not selected_booking_credit_terms_checkout_field.empty:
                            for i, field in selected_booking_credit_terms_checkout_field.iterrows():
                                column = Models.checkout_field_column_mapper.get(field.FieldID)
                                if column is not None and field.Value is not None:
                                    setattr(gbq_booking_booker_tmp, column, field.Value)
                                    
                        if row.bookingstatus == 'cancel_multi_payment' or row.bookingstatus == 'cancel_credit':
                            if selected_booking_payment_credit_terms.ApprovalStatus.values[0] == 'reject':
                                approval_status = 'reject'
                            else:
                                approval_status = 'booker_cancelled'
                        elif row.bookingstatus == 'success':
                            if selected_booking_payment_credit_terms.ApprovalStatus.values[0] == 'approve':
                                if selected_booking_payment_credit_terms.ApprovalBy.values[0] == 'cron':
                                    approval_status = 'no_action'
                                    if row.allowapproveaftercheckin or row.checkin.strftime('%Y-%m-%d') == date_now.astimezone(pytz.timezone('Asia/Bangkok')).strftime('%Y-%m-%d'):
                                        if selected_booking_payment_credit_terms.ApprovalOTP.values[0] and not selected_booking_payment_credit_terms.ApprovalOTP.values[0].isspace():
                                            approval_status = 'last_minute'
                                
                                elif selected_booking_payment_credit_terms.ApprovalBy.values[0] == 'check_in_date':
                                    if selected_booking_payment_credit_terms.ApprovalOTP.values[0] and not selected_booking_payment_credit_terms.ApprovalOTP.values[0].isspace():
                                        approval_status = 'last_minute'
                                    else:
                                        approval_status = 'no_action'
                                
                                elif selected_booking_payment_credit_terms.ApprovalBy.values[0] == 'supervisor':
                                    approval_status = 'approve'
                            
                            elif selected_booking_payment_credit_terms.ApprovalStatus.values[0] == 'pending':
                                if row.checkin.strftime('%Y-%m-%d') == date_now.astimezone(pytz.timezone('Asia/Bangkok')).strftime('%Y-%m-%d') and selected_booking_payment_credit_terms.ApprovalOTP.values[0] and not selected_booking_payment_credit_terms.ApprovalOTP.values[0].isspace():
                                    approval_status = 'last_minute'
                                else:
                                    approval_status = 'pending'
                        gbq_booking_booker_tmp.ApprovalStatus = approval_status
                        gbq_booking_booker_tmp.SupervisorEmail = selected_booking_payment_credit_terms.ApprovalEmail.values[0] if \
                                selected_booking_payment_credit_terms.ApprovalEmail.values[0] is not None else \
                                selected_booking_payment_credit_terms.BookerReporterEmail.values[0]
                        gbq_booking_booker_tmp.ApproverName = selected_booking_payment_credit_terms.ApprovalNameEN.values[0]
                        gbq_booking_booker_tmp.ApproverEmail = selected_booking_payment_credit_terms.ApprovalEmail.values[0]
                        gbq_booking_booker_tmp.ReporterEmail = selected_booking_payment_credit_terms.BookerReporterEmail.values[0]
                        gbq_booking_booker_tmp.BookerBudget = selected_booking_payment_credit_terms.BookerBudget.values[0]

                gbq_booking_booker_tmp.Quantity = selected_booking_roomtype_condition.Quantity.sum()
                gbq_booking_booker_tmp.BookerDepartment = selected_booking_booker_row['BookerDepartment']
                gbq_booking_booker_tmp.ISO2 = selected_booking_hotel.CountryCodeISO2.values[0]

                booker_vat = selected_booking_payment.Vat.values[0] if pd.notna(selected_booking_payment.Vat.values[0]) else 0
                total_selling_price = round_half_up(selected_booking_payment_all_booker.ChargeTotal.sum() * booker_exchange_rate)
                total_selling_ex_vat_amount = round_half_up(total_selling_price / (1 + (booker_vat / 100)))
                total_selling_vat_amount = round_half_up( total_selling_price - total_selling_ex_vat_amount if booker_vat > 0 else None)

                selling_price = round_half_up(selected_booking_payment.ChargeTotal.sum() * booker_exchange_rate)   
                selling_ex_vat_amount = round_half_up(selling_price / (1 + (booker_vat / 100)))
                selling_vat_amount = round_half_up(selling_price - selling_ex_vat_amount if booker_vat > 0 else None)

                supplier_vat = selected_booking_supplier.Vat.values[0] if pd.notna(selected_booking_supplier.Vat.values[0]) else 0
                supplier_ex_rate = selected_booking_supplier.SupplierExchangeRate.values[0] if pd.notna(selected_booking_supplier.SupplierExchangeRate.values[0]) else 1
                total_cost = round_half_up(selected_booking_roomtype_condition.TotalCost.sum())
                total_cost_ex_vat_amount = round_half_up(total_cost / (1 + (supplier_vat / 100)))
                total_cost_vat_amount = round_half_up(total_cost - total_cost_ex_vat_amount if supplier_vat > 0 else None)

                total_convert_total_cost = round_half_up(total_cost * supplier_ex_rate)
                total_converted_cost_ex_vat = round_half_up(total_convert_total_cost / (1 + (supplier_vat / 100)))
                total_converted_vat = round_half_up(total_convert_total_cost - total_converted_cost_ex_vat if supplier_vat > 0 else None)

                total_profit_amount = round_half_up(total_selling_ex_vat_amount - total_converted_cost_ex_vat)
                total_profit_percent = round_half_up((total_profit_amount / total_selling_ex_vat_amount) * 100)

                if (booker_index < len(selected_booking_booker.index) - 1):
                    booker_cost = round_half_up(total_cost * (selling_price / total_selling_price))
                    booker_cost_sum += booker_cost
                else:
                    booker_cost = total_cost - booker_cost_sum

                booker_cost_ex_vat_amount = round_half_up(booker_cost / (1 + (supplier_vat / 100)))
                booker_cost_vat_amount = round_half_up(booker_cost - booker_cost_ex_vat_amount if supplier_vat > 0 else None)
                booker_convert_total_cost = round_half_up(booker_cost * supplier_ex_rate)
                booker_converted_cost_ex_vat = round_half_up(booker_convert_total_cost / (1 + (supplier_vat / 100)))
                booker_converted_vat = round_half_up(booker_convert_total_cost - booker_converted_cost_ex_vat if supplier_vat > 0 else None)
                booker_profit_amount = round_half_up(selling_ex_vat_amount - booker_converted_cost_ex_vat)
                booker_profit_percent = 0 if selling_ex_vat_amount == 0 else round_half_up((booker_profit_amount / selling_ex_vat_amount) * 100)
                commission = round_half_up(selected_booking_roomtype_condition.Commission.sum())

                if not selected_booking_cancel.empty:
                    selected_booking_cancel['TotalSupplierChargeAmount'] = selected_booking_cancel['SupplierChargeAmount'] * selected_booking_cancel['Quantity']
                    selected_booking_cancel['TotalChargeBookerAmount'] = selected_booking_cancel['ChargeBookerAmount'] * selected_booking_cancel['Quantity']
                    booker_exchange_rate = selected_booking_cancel.BookerExchangeRate.values[0] if pd.notna(selected_booking_cancel.BookerExchangeRate.values[0]) else 1
                    supplier_exchange_rate = selected_booking_cancel.SupplierExchangeRate.values[0] if pd.notna(selected_booking_cancel.SupplierExchangeRate.values[0]) else 1

                    total_cancellation_cost = round_half_up(selected_booking_cancel.TotalSupplierChargeAmount.sum() * supplier_exchange_rate)
                    total_cancellation_cost_ex_vat = round_half_up(total_cancellation_cost / (1 + (supplier_vat / 100)))
                    total_cancellation_cost_vat = round_half_up(total_cancellation_cost - total_cancellation_cost_ex_vat if supplier_vat > 0 else None)

                    if booker_index < len(selected_booking_booker.index) - 1:
                        booker_cancellation_cost = round_half_up(total_cancellation_cost * (selling_price / total_selling_price))
                        booker_cancellation_cost_sum += booker_cancellation_cost
                    else:
                        booker_cancellation_cost = total_cancellation_cost - booker_cancellation_cost_sum

                    booker_cancellation_cost_ex_vat = round_half_up(booker_cancellation_cost / (1 + (supplier_vat / 100)))
                    booker_cancellation_cost_vat = round_half_up(booker_cancellation_cost - booker_cancellation_cost_ex_vat if supplier_vat > 0 else None)

                    cancellation_fee = round_half_up(selected_booking_payment.CancelChargeTotal.sum() * booker_exchange_rate)
                    cancellation_fee_ex_vat = round_half_up(cancellation_fee / (1 + (booker_vat / 100)))
                    cancellation_fee_vat = round_half_up(cancellation_fee - cancellation_fee_ex_vat if booker_vat > 0 else None)

                    gbq_booking_booker_tmp.CancellationCost = booker_cancellation_cost
                    gbq_booking_booker_tmp.CancellationCostExVat = booker_cancellation_cost_ex_vat
                    gbq_booking_booker_tmp.CancellationCostVat = booker_cancellation_cost_vat

                    gbq_booking_booker_tmp.CancellationFee = cancellation_fee
                    gbq_booking_booker_tmp.CancellationFeeExVat = cancellation_fee_ex_vat
                    gbq_booking_booker_tmp.CancellationFeeVat = cancellation_fee_vat

                gbq_booking_booker_tmp.TotalSellingPrice = selling_price
                gbq_booking_booker_tmp.SellingVat = selling_vat_amount
                gbq_booking_booker_tmp.SellingExVAT = selling_ex_vat_amount

                gbq_booking_booker_tmp.TotalCost = booker_cost
                gbq_booking_booker_tmp.CostExVAT = booker_cost_ex_vat_amount
                gbq_booking_booker_tmp.SupplierVAT = booker_cost_vat_amount

                gbq_booking_booker_tmp.ConvertedTotalCost = booker_convert_total_cost
                gbq_booking_booker_tmp.ConvertedCostExVAT = booker_converted_cost_ex_vat
                gbq_booking_booker_tmp.ConvertedVAT = booker_converted_vat

                gbq_booking_booker_tmp.ProfitAmount = booker_profit_amount
                gbq_booking_booker_tmp.ProfitPercent = booker_profit_percent

                gbq_booking_booker_tmp.Commission = commission

                gbq_booking_booker_tmp.Contribution = Models.switcher_contribution.get(selected_booking_booker_row['CompanyContribution'], None)
                gbq_booking_booker_tmp.CompanySize = Models.switcher_company_size.get(selected_booking_booker_row['CompanySize'], None)
                gbq_booking_booker_tmp.IndustryTypeName = selected_booking_booker_row['CompanyIndustryTypeSubName']
                gbq_booking_booker_tmp.ParentCompanyName = selected_booking_booker_row['RootCompanyNameEN']
                gbq_booking_booker_tmp.AccountManagerOwner = selected_booking_booker_row['CompanyOwner']
                gbq_booking_booker_tmp.CompanyID = selected_booking_payment.BookerCompanyID.values[0]
                gbq_booking_booker_tmp.ContractingManagerOwner = None if selected_booking_hotel.ContractingManagerOwner.empty else \
                    selected_booking_hotel.ContractingManagerOwner.values[0]
                gbq_booking_booker_tmp.ProductID = selected_booking_hotel.HotelID.values[0]
                gbq_booking_booker_tmp.SupplierID = selected_booking_supplier.SupplierID.values[0]
                gbq_booking_booker_tmp.BookInAdvance = days_between(row.checkin, row.bookingdate)
                gbq_booking_booker_tmp.RoomNight = days_between(row.checkout, row.checkin) * selected_booking_roomtype_condition.Quantity.sum()
                gbq_booking_booker_tmp.AverageSellingPricePerNightTHB = round_half_up(selling_price / gbq_booking_booker_tmp.RoomNight)

                gbq_booking_booker_tmp.UserBookerInternalOrder = selected_booking_booker_row.BookerInternalOrder
                gbq_booking_booker_tmp.UserBookerCostCenterName = selected_booking_booker_row.BookerCostCenterName
                gbq_booking_booker_tmp.UserBookerCostCenterCode = selected_booking_booker_row.BookerCostCenterCode
                gbq_booking_booker_tmp.UserBookerBusinessUnit = selected_booking_booker_row.BookerBusinessUnit
                gbq_booking_booker_tmp.UserBookerBusinessArea = selected_booking_booker_row.BookerBusinessArea
                gbq_booking_booker_tmp.UserBookerBusinessPlace = selected_booking_booker_row.BookerBusinessPlace
                gbq_booking_booker_tmp.UserBookerProfitCenter = selected_booking_booker_row.BookerProfitCenter
                gbq_booking_booker_tmp.UserBookerLevel = selected_booking_booker_row.BookerLevel
                gbq_booking_booker_tmp.UserBookerDepartmentID = selected_booking_booker_row.BookerDepartmentID
                gbq_booking_booker_tmp.UserBookerDepartment = selected_booking_booker_row.BookerDepartment
                gbq_booking_booker_tmp.UserBookerPosition = selected_booking_booker_row.BookerPosition
                gbq_booking_booker_tmp.UserBookerEmployeeID = selected_booking_booker_row.BookerEmployeeID
                gbq_booking_booker_tmp.UserBookerTaxCode = selected_booking_booker_row.BookerTaxCode
                gbq_booking_booker_tmp.PaymentMethod = row.paymenttype

                if booker_index == 0:
                    gbq_booking_summary_tmp = GbqBookingSummary()
                    gbq_booking_summary_tmp.__dict__ = gbq_booking_booker_tmp.__dict__.copy()

                    if not selected_booking_cancel.empty:
                        total_cancellation_fee = round_half_up(selected_booking_payment_all_booker.CancelChargeTotal.sum() * booker_exchange_rate)
                        total_cancellation_fee_ex_vat = round_half_up(total_cancellation_fee / (1 + (booker_vat / 100)))
                        total_cancellation_fee_vat = round_half_up(total_cancellation_fee - total_cancellation_fee_ex_vat if booker_vat > 0 else None)

                        gbq_booking_summary_tmp.CancellationCost = total_cancellation_cost
                        gbq_booking_summary_tmp.CancellationCostExVat = total_cancellation_cost_ex_vat
                        gbq_booking_summary_tmp.CancellationCostVat = total_cancellation_cost_vat

                        gbq_booking_summary_tmp.CancellationFee = total_cancellation_fee
                        gbq_booking_summary_tmp.CancellationFeeExVat = total_cancellation_fee_ex_vat
                        gbq_booking_summary_tmp.CancellationFeeVat = total_cancellation_fee_vat

                    gbq_booking_summary_tmp.TotalSellingPrice = total_selling_price
                    gbq_booking_summary_tmp.SellingVat = total_selling_vat_amount
                    gbq_booking_summary_tmp.SellingExVAT = total_selling_ex_vat_amount
                    gbq_booking_summary_tmp.SellingPriceCreditTerms = round_half_up(selected_credit_terms_all_booker.ChargeTotal.sum() * booker_exchange_rate)

                    gbq_booking_summary_tmp.TotalCost = total_cost
                    gbq_booking_summary_tmp.CostExVAT = total_cost_ex_vat_amount
                    gbq_booking_summary_tmp.SupplierVAT = total_cost_vat_amount

                    gbq_booking_summary_tmp.ConvertedTotalCost = total_convert_total_cost
                    gbq_booking_summary_tmp.ConvertedCostExVAT = total_converted_cost_ex_vat
                    gbq_booking_summary_tmp.ConvertedVAT = total_converted_vat

                    gbq_booking_summary_tmp.ProfitAmount = total_profit_amount
                    gbq_booking_summary_tmp.ProfitPercent = total_profit_percent
                    gbq_booking_summary_tmp.PaymentMethodDetail = ' + '.join(sorted(list(dict.fromkeys(selected_booking_payment.PaymentType.values)), key=cmp_to_key(compare)))
                    gbq_booking_summary_list.append(gbq_booking_summary_tmp.__dict__)

                gbq_booking_booker_list.append(gbq_booking_booker_tmp.__dict__)

        except Exception as e:
            traceback.print_exc()
            print('[ETL BOOKING DASHBOARD] Exception with (%s): ' % row.bookingid, e)
            error_booking_id.append(row.bookingid)
        return GbqBookingDashboardModel(gbq_booking_summary_list, gbq_booking_booker_list, error_booking_id)

    def handler_product(self, event, context):
        print("[ETL BOOKING DASHBOARD][PRODUCT][REQUEST] => ", event)
        productIds = list(dict.fromkeys(event['productIds'])) if 'productIds' in event else None
        if productIds is not None and not all(isinstance(x, (int, numpy.int64)) for x in productIds):
            return {
                'error': {
                    'code': '4000',
                    'message': 'Invalid request'
                }
            }
        if productIds is not None:
            host = self._config_client.get_attribute('database.host')
            user = self._config_client.get_attribute('database.user')
            password = self._config_client.get_attribute('database.password')
            act_db = self._config_client.get_attribute('database.act')

            act_repository = pymssql.connect(
                host=host,
                database=act_db,
                user=user,
                password=password
            )
            stmt = """SELECT h.HotelID, h.NameEN, h.NameTH, h.AddressEN, h.AddressTH, s.NameEN AS State, h.Postcode, c.TitleEn AS Country, h.LatestRate AS LatestRate
                    FROM dbo.Hotel h
                    LEFT JOIN dbo.Destination d on h.DestinationID = d.DestinationID 
                    LEFT JOIN dbo.Country c on c.CountryID = d.CountryID 
                    LEFT JOIN dbo.HotelState hs on hs.HotelID = h.HotelID 
                    LEFT JOIN dbo.State s on s.StateID = hs.StateID 
                    WHERE h.HotelID IN (%s)"""
            dfHotel = get_df_from_query(stmt=stmt, connection=act_repository, list=productIds)
            print("[ETL BOOKING DASHBOARD][PRODUCT] query result size dfHotel => ", len(dfHotel.index))

            gbq_product_list = []
            for i, val in dfHotel.iterrows():
                gbq_product_tmp = GbqProduct()
                gbq_product_tmp.ProductID = val['HotelID']
                gbq_product_tmp.ProductName = val['NameEN']
                gbq_product_tmp.ProductNameTH = val['NameTH']
                gbq_product_tmp.Address = val['AddressEN']
                gbq_product_tmp.AddressTH = val['AddressTH']
                gbq_product_tmp.State = val['State']
                gbq_product_tmp.Postcode = val['Postcode']
                gbq_product_tmp.Country = val['Country']
                gbq_product_tmp.ExpireRate = val['LatestRate']
                gbq_product_list.append(gbq_product_tmp.__dict__)
            df_gbq_product = pd.DataFrame(data=gbq_product_list, columns=gbq_product_list[0].keys())
            try:
                QUERY = ('DELETE FROM ascend-travel-prod.{0}.{1} WHERE ProductID IN ({2})'.format(
                        self._gbq_group_table,
                        self._gbq_product_table,
                        ','.join(map(str, df_gbq_product['ProductID'].tolist()))))

                gbq_client = bigquery.Client(
                    credentials=self._credentials,
                    project=self._credentials.project_id,
                )
                query_job = gbq_client.query(QUERY)  # API request
                query_job.result()
            except Exception as e:
                print('[ETL BOOKING DASHBOARD][PRODUCT] Exception with : ', e)
            df_gbq_product = df_gbq_product.astype({'ProductID': 'int64'})
            df_gbq_product.to_gbq(destination_table='%s.%s' % (self._gbq_group_table, self._gbq_product_table),
                                  if_exists='append',
                                  project_id=self._credentials.project_id, credentials=self._credentials,
                                  table_schema = [{'name':'ExpireRate','type': 'DATE'}])
            act_repository.close()

    def handler_destination(self, event, context):
        print("[ETL BOOKING DASHBOARD][DESTINATION][REQUEST] => ", event)
        destinationIds = list(dict.fromkeys(event['destinationIds'])) if 'destinationIds' in event else None
        if destinationIds is not None and not all(isinstance(x, (int, numpy.int64)) for x in destinationIds):
            return {
                'error': {
                    'code': '4000',
                    'message': 'Invalid request'
                }
            }
        if destinationIds is not None:
            host = self._config_client.get_attribute('database.host')
            user = self._config_client.get_attribute('database.user')
            password = self._config_client.get_attribute('database.password')
            act_db = self._config_client.get_attribute('database.act')

            act_repository = pymssql.connect(
                host=host,
                database=act_db,
                user=user,
                password=password
            )
            stmt = "SELECT DestinationID, TitleEN, TitleTH FROM dbo.Destination WHERE DestinationID IN (%s)"
            dfDestination = get_df_from_query(stmt=stmt, connection=act_repository, list=destinationIds)
            print("[ETL BOOKING DASHBOARD][DESTINATION] query result size dfDestination => ", len(dfDestination.index))

            dbq_destination_list = []
            for i, val in dfDestination.iterrows():
                gbq_destination_tmp = GbqDestination()
                gbq_destination_tmp.DestinationID = val['DestinationID']
                gbq_destination_tmp.DestinationName = val['TitleEN']
                dbq_destination_list.append(gbq_destination_tmp.__dict__)
            df_gbq_destination = pd.DataFrame(data=dbq_destination_list, columns=dbq_destination_list[0].keys())
            try:
                QUERY = (
                    'DELETE FROM ascend-travel-prod.{0}.{1} WHERE DestinationID IN ({2})'.format(
                        self._gbq_group_table,
                        self._gbq_destination_table,
                        ','.join(map(str, df_gbq_destination['DestinationID'].tolist()))))
                gbq_client = bigquery.Client(
                    credentials=self._credentials,
                    project=self._credentials.project_id,
                )
                query_job = gbq_client.query(QUERY)  # API request
                query_job.result()
            except Exception as e:
                print('[ETL BOOKING DASHBOARD][DESTINATION] Exception with : ', e)
            df_gbq_destination = df_gbq_destination.astype({'DestinationID': 'int64'})
            df_gbq_destination.to_gbq(destination_table='%s.%s' % (self._gbq_group_table, self._gbq_destination_table),
                                      if_exists='append',
                                      project_id=self._credentials.project_id, credentials=self._credentials)
            act_repository.close()

    def handler_supplier(self, event, context):
        print("[ETL BOOKING DASHBOARD][SUPPLIER][REQUEST] => ", event)
        supplierIds = list(dict.fromkeys(event['supplierIds'])) if 'supplierIds' in event else None
        if supplierIds is not None and not all(isinstance(x, (int, numpy.int64)) for x in supplierIds):
            return {
                'error': {
                    'code': '4000',
                    'message': 'Invalid request'
                }
            }
        if supplierIds is not None:
            host = self._config_client.get_attribute('database.host')
            user = self._config_client.get_attribute('database.user')
            password = self._config_client.get_attribute('database.password')
            supplier_db = self._config_client.get_attribute('database.supplier')

            supplier_repository = pymssql.connect(
                host=host,
                database=supplier_db,
                user=user,
                password=password
            )
            stmt = "SELECT SupplierID, Name FROM dbo.Supplier WHERE SupplierID IN (%s)"
            dfSupplier = get_df_from_query(stmt=stmt, connection=supplier_repository, list=supplierIds)
            print("[ETL BOOKING DASHBOARD][SUPPLIER] query result size dfSupplier => ", len(dfSupplier.index))

            gbq_supplier_list = []
            for i, val in dfSupplier.iterrows():
                df_gbq_supplier_tmp = GbqSupplier()
                df_gbq_supplier_tmp.SupplierID = val['SupplierID']
                df_gbq_supplier_tmp.SupplierName = val['Name']
                gbq_supplier_list.append(df_gbq_supplier_tmp.__dict__)
            df_gbq_supplier = pd.DataFrame(data=gbq_supplier_list, columns=gbq_supplier_list[0].keys())
            try:
                QUERY = (
                    'DELETE FROM ascend-travel-prod.{0}.{1} WHERE SupplierID IN ({2})'.format(
                        self._gbq_group_table,
                        self._gbq_supplier_table,
                        ','.join(map(str,df_gbq_supplier['SupplierID'].tolist()))))

                gbq_client = bigquery.Client(
                    credentials=self._credentials,
                    project=self._credentials.project_id,
                )
                query_job = gbq_client.query(QUERY)  # API request
                query_job.result()
            except Exception as e:
                print('[ETL BOOKING DASHBOARD][SUPPLIER] Exception with : ', e)
            df_gbq_supplier = df_gbq_supplier.astype({'SupplierID': 'int64'})
            df_gbq_supplier.to_gbq(destination_table='%s.%s' % (self._gbq_group_table, self._gbq_supplier_table),
                                   if_exists='append',
                                   project_id=self._credentials.project_id, credentials=self._credentials)
            supplier_repository.close()

    def handler_company_retention(self, event, context):
        print("[ETL BOOKING DASHBOARD][COMPANY RETENTION][REQUEST] => ", event)
        companyIds = list(dict.fromkeys(event['companyIds'])) if 'companyIds' in event else None
        if companyIds is not None and not all(isinstance(x, (int, numpy.int64)) for x in companyIds):
            return {
                'error': {
                    'code': '4000',
                    'message': 'Invalid request'
                }
            }
        delete_all = False
        host = self._config_client.get_attribute('database.host')
        user = self._config_client.get_attribute('database.user')
        password = self._config_client.get_attribute('database.password')
        company_db = self._config_client.get_attribute('database.company')

        company_repository = pymssql.connect(
            host=host,
            database=company_db,
            user=user,
            password=password
        )
        if companyIds is None:
            delete_all = True
            stmt = "SELECT CompanyID, NameEN, NameTH, IndustryType, IndustryTypeSubName, Contribution, CustomerType FROM dbo.Company WHERE SupplierID IS NULL"
            dfCompany = pd.read_sql_query(stmt, company_repository)

        else:
            stmt = "SELECT CompanyID, NameEN, NameTH, IndustryType, IndustryTypeSubName, Contribution, CustomerType FROM dbo.Company WHERE SupplierID IS NULL AND CompanyID IN (%s)"
            dfCompany = get_df_from_query(stmt=stmt, connection=company_repository, list=companyIds)
        print("[ETL BOOKING DASHBOARD][COMPANY RETENTION] query result size dfCompany => ", len(dfCompany.index))
        if dfCompany.empty:
            return {
                'error': {
                    'code': '4004',
                    'message': 'Company not found.'
                }
            }
        companyIds = list(dict.fromkeys(dfCompany['CompanyID'].tolist()))

        host = self._config_client.get_attribute('database.host')
        user = self._config_client.get_attribute('database.user')
        password = self._config_client.get_attribute('database.password')
        booking_db = self._config_client.get_attribute('database.booking')

        booking_repository = pymssql.connect(
            user=user,
            password=password,
            database=booking_db,
            host=host
        )
        today = datetime.today()
        date_format = '%Y-%m-%d %H:%M:%S'
        stmt = "SELECT DISTINCT YEAR(BookingDate) year, MONTH(BookingDate) month, BookerCompanyID, CompanyNameEN " \
               "FROM dbo.Booking b JOIN dbo.BookingBooker bb ON b.BookingID = bb.BookingID " \
               "JOIN dbo.BookingBookerCompanyInformation bbc ON bb.BookingBookerID = bbc.BookingBookerID " \
               "WHERE (BookingStatus = 'success' OR BookingStatus = 'cancel_credit' OR BookingStatus = 'cancel_non_credit' OR BookingStatus = 'cancel_multi_payment') " \
               "AND BookerCompanyID IN (%s) and BookingDate BETWEEN '{0}' AND '{1}'".format(
                (today.replace(day=1, hour=0, minute=0, second=0, microsecond=0) - dateutil.relativedelta.relativedelta(months=5, hours=7)).strftime(date_format),
                today.strftime(date_format))

        dfBookingDate = get_df_from_query(stmt=stmt, connection=booking_repository, list=companyIds)
        print("[ETL BOOKING DASHBOARD][COMPANY RETENTION] query result size dfBookingDate => ", len(dfBookingDate.index))

        dfBookingDate['day'] = 1
        dfBookingDate['BookingDate'] = pd.to_datetime(dfBookingDate[['year', 'month', 'day']])

        month = [(datetime.today() - dateutil.relativedelta.relativedelta(months=n)).strftime('%b %Y') for n in range(0, 6, 1)]
        errorCompanyIds = []
        gbq_company_retention_list = []
        for i, val in enumerate(companyIds):
            print('[ETL BOOKING DASHBOARD][COMPANY RETENTION] company id => ', val)
            try:
                active = 0
                max_active = 0
                transaction = []
                for j, val_m in enumerate(month):
                    selectedMonth = dfBookingDate[
                        (pd.to_datetime(dfBookingDate['BookingDate']).dt.strftime('%b %Y') == val_m) & (
                                dfBookingDate['BookerCompanyID'] == val)]
                    if not selectedMonth.empty:
                        active += 1
                        transaction.append(
                            '[%s]' % pd.to_datetime(selectedMonth.BookingDate.values[0]).strftime('%b').upper())
                    else:
                        active = 0
                        transaction.append('[--]')

                    max_active = active if active > max_active else max_active
                df_gbq_company_tmp = GbqCompany()
                df_gbq_company_tmp.CompanyID = val
                df_gbq_company_tmp.LastTransaction = '--'.join(map(str, transaction))
                df_gbq_company_tmp.ActiveTransaction = 'Inactive' if max_active == 0 else 'Active %s M' % max_active
                gbq_company_retention_list.append(df_gbq_company_tmp.__dict__)
            except Exception as e:
                print('[ETL BOOKING DASHBOARD][COMPANY RETENTION] Exception with : ', e)
                errorCompanyIds.append(val)
        df_gbq_company_retention = pd.DataFrame(data=gbq_company_retention_list, columns=gbq_company_retention_list[0].keys())

        gbq_company_cols = ['CompanyID', 'CompanyName', 'IndustryType', 'IndustryTypeSubName', 'Contribution']
        df_gbq_company = pd.DataFrame(columns=gbq_company_cols)
        df_gbq_company['CompanyID'] = dfCompany['CompanyID']
        df_gbq_company['CompanyName'] = dfCompany['NameEN']
        df_gbq_company['IndustryType'] = dfCompany['IndustryType']
        df_gbq_company['IndustryTypeSubName'] = dfCompany['IndustryTypeSubName']
        df_gbq_company['Contribution'] = dfCompany['Contribution']
        df_gbq_company['CustomerType'] = dfCompany['CustomerType']
        gbq_client = bigquery.Client(
            credentials=self._credentials,
            project=self._credentials.project_id,
        )
        try:
            QUERY = (
                'DELETE FROM ascend-travel-prod.{0}.{1} WHERE CompanyID IN ({2})'.format(
                    self._gbq_group_table,
                    self._gbq_company_table,
                    ','.join(map(str, df_gbq_company['CompanyID'].tolist()))))

            query_job = gbq_client.query(QUERY)  # API request
            query_job.result()
        except Exception as e:
            print('[ETL BOOKING DASHBOARD][COMPANY RETENTION] Exception with : ', e)
        df_gbq_company = df_gbq_company.astype({'CompanyID': 'int64'})
        df_gbq_company.to_gbq(destination_table='%s.%s' % (self._gbq_group_table, self._gbq_company_table),
                              if_exists='append',
                              project_id=self._credentials.project_id, credentials=self._credentials)

        try:
            if delete_all:
                QUERY = ('DELETE FROM ascend-travel-prod.%s.%s  WHERE true' % (self._gbq_group_table, self._gbq_company_retention_table))
            else:
                QUERY = (
                    'DELETE FROM ascend-travel-prod.{0}.{1} WHERE CompanyID IN ({2})'.format(
                        self._gbq_group_table,
                        self._gbq_company_retention_table,
                        ','.join(map(str, df_gbq_company_retention['CompanyID'].tolist()))))

            query_job = gbq_client.query(QUERY)  # API request
            query_job.result()
        except Exception as e:
            print('[ETL BOOKING DASHBOARD][COMPANY RETENTION] Exception with : ', e)
        df_gbq_company_retention = df_gbq_company_retention.astype({'CompanyID': 'int64'})
        df_gbq_company_retention.to_gbq(
            destination_table='%s.%s' % (self._gbq_group_table, self._gbq_company_retention_table),
            if_exists='append',
            project_id=self._credentials.project_id, credentials=self._credentials)

        companyIds = df_gbq_company_retention['CompanyID'].tolist()
        booking_repository.close()
        company_repository.close()
        return {
            'companyIds': companyIds,
            'errorCompanyIds': errorCompanyIds
        }

    def df_gbq_booking_booker_to_gbq(self, gbq_booking_booker_list):
        df_gbq_booking_booker = pd.DataFrame(data=gbq_booking_booker_list,
                                             columns=getattr(GbqBookingBooker, '_defaults'))
        try:
            QUERY = ('DELETE FROM ascend-travel-prod.{0}.{1} WHERE BookingID IN ({2})'.format(
                self._gbq_group_table,
                self._gbq_booking_booker_table,
                ','.join(map(str,df_gbq_booking_booker['BookingID'].tolist()))))
            gbq_client = bigquery.Client(
                credentials=self._credentials,
                project=self._credentials.project_id,
            )
            query_job = gbq_client.query(QUERY)  # API request
            query_job.result()
        except Exception as e:
            print('[ETL BOOKING DASHBOARD] Exception with : ', e)

        df_gbq_booking_booker.BookerBudget.fillna(value=numpy.nan, inplace=True)
        df_gbq_booking_booker = df_gbq_booking_booker.astype(Models.big_query_table_type)
        df_gbq_booking_booker.to_gbq(
            destination_table='%s.%s' % (self._gbq_group_table, self._gbq_booking_booker_table),
            if_exists='append',
            project_id=self._credentials.project_id, credentials=self._credentials,
            table_schema=Models.booking_booker_schema)

    def df_gbq_booking_summary_to_gbq(self, gbq_booking_summary_list):
        df_gbq_booking_summary = pd.DataFrame(data=gbq_booking_summary_list,
                                              columns=getattr(GbqBookingSummary, '_defaults'))
        try:
            QUERY = ('DELETE FROM ascend-travel-prod.{0}.{1} WHERE BookingID IN ({2})'.format(
                self._gbq_group_table,
                self._gbq_booking_summary_table,
                ','.join(map(str, df_gbq_booking_summary['BookingID'].tolist()))))
            gbq_client = bigquery.Client(
                credentials=self._credentials,
                project=self._credentials.project_id,
            )
            query_job = gbq_client.query(QUERY)  # API request
            query_job.result()
        except Exception as e:
            print('[ETL BOOKING DASHBOARD] Exception with : ', e)

        df_gbq_booking_summary.to_gbq(
            destination_table='%s.%s' % (self._gbq_group_table, self._gbq_booking_summary_table),
            if_exists='append',
            project_id=self._credentials.project_id, credentials=self._credentials,
            table_schema=Models.booking_summary_schema)
    
    def df_gbq_booking_operation_to_gbq(self, gbq_booking_operation_list):
        gbq_booking_operation = pd.DataFrame(data=gbq_booking_operation_list, columns=gbq_booking_operation_list[0].keys())
        try:
            QUERY = ('DELETE FROM ascend-travel-prod.{0}.{1} WHERE BookingID IN ({2})'.format(
                self._gbq_group_table,
                self._gbq_booking_operation_table,
                ','.join(map(str, gbq_booking_operation['BookingID'].tolist()))))
            gbq_client = bigquery.Client(
                credentials=self._credentials,
                project=self._credentials.project_id,
            )
            query_job = gbq_client.query(QUERY)  # API request
            query_job.result()
        except Exception as e:
            print('[ETL BOOKING DASHBOARD] Exception with : ', e)

        gbq_booking_operation.to_gbq(
            destination_table='%s.%s' % (self._gbq_group_table, self._gbq_booking_operation_table),
            if_exists='append',
            project_id=self._credentials.project_id, credentials=self._credentials,
            table_schema=Models.booking_operation_schema)

    def get_sqs_message(self, queue_url=None):
        sqs = boto3.client('sqs',
                           region_name=self._region_name,
                           aws_access_key_id=self._aws_access_key_id,
                           aws_secret_access_key=self._aws_secret_access_key)

        bookingIds = []
        queue_url = self._queue_url_etl_booking if queue_url == None else queue_url

        while True:
            messages = sqs.receive_message(QueueUrl=queue_url, MaxNumberOfMessages=10)
            if 'Messages' in messages:
                for message in messages['Messages']:
                    if message['Body'] not in bookingIds:
                        bookingIds.append(message['Body'])
                    sqs.delete_message(QueueUrl=queue_url, ReceiptHandle=message['ReceiptHandle'])
            else:
                print('[GET SQS MESSAGE] Messages on queue: ', bookingIds)
                print('[GET SQS MESSAGE] Queue is now empty')
                break

        return bookingIds

    def push_message_recheck_etl_booking(self, booking_ids, timestamp):
        sqs = boto3.client('sqs',
                           region_name=self._region_name,
                           aws_access_key_id=self._aws_access_key_id,
                           aws_secret_access_key=self._aws_secret_access_key)

        sqs.send_message(
            QueueUrl=self._queue_url_etl_booking_validation,
            MessageBody=json.dumps({"bookingIds": booking_ids, "timestamp": timestamp.isoformat()}),
            MessageGroupId='etl-booking-validation'
        )

    def compute_chunksize(self, iterable_size):
        if iterable_size == 0:
            return 0
        chunksize, extra = divmod(iterable_size, POOL_SIZE * 4)
        if extra:
            chunksize += 1
        return chunksize

    def handler(self, event): # event = body request
        print("[ETL BOOKING DASHBOARD][REQUEST] => ", event)
        booking_ids = list(dict.fromkeys(event['bookingIds'])) if 'bookingIds' in event else None
        if booking_ids is not None and not all(isinstance(x, (int, numpy.int64)) for x in booking_ids):
            return {
                'error': {
                    'code': '4000',
                    'message': 'Invalid request'
                }
            }

        dateFrom = event['dateFrom'] if 'dateFrom' in event else None
        dateTo = event['dateTo'] if 'dateTo' in event else None
        dateType = event['dateType'] if 'dateType' in event else ETLDateType.booking_date
        dfBooking = None
        date_now = datetime.utcnow()
        
        # setting database
        host = self._config_client.get_attribute('database.host')
        user = self._config_client.get_attribute('database.user')
        password = self._config_client.get_attribute('database.password')
        booking_db = self._config_client.get_attribute('database.booking')

        #connect database
        booking_repository = pymssql.connect(
            user=user,
            password=password,
            database=booking_db,
            host=host
        )

        stmt = """SELECT bookingid, bookingstatus, bookingdate, canceldate, purpose, checkin, checkout, updatedby,
            updateddate, financialstatus, langcodeiso2, unearndate, revenuedate, promotioncode, cancelby,
            cancelemail, cancelreason, payondate, paymenttype, allowapproveaftercheckin
            FROM Booking
            WHERE BookingStatus IN ('success', 'cancel_credit', 'cancel_non_credit', 'cancel_booking', 'cancel_multi_payment')
            AND {condition}
            """

        if dateFrom is not None and dateTo is not None:
            if dateType == ETLDateType.booking_date:
                stmt = stmt.format(condition="""BookingDate BETWEEN '{0}' AND '{1}'""".format(dateFrom, dateTo))
            elif dateType == ETLDateType.checkin_date:
                stmt = stmt.format(condition="""CheckIn BETWEEN '{0}' AND '{1}'""".format(dateFrom, dateTo))
            dfBooking = pd.read_sql_query(stmt, booking_repository)
        
        elif booking_ids is not None:
            stmt = stmt.format(condition='BookingID IN (%s)')
            dfBooking = get_df_from_query(stmt=stmt, connection=booking_repository, list=booking_ids)
        
        else:
            print("[ETL BOOKING DASHBOARD] Empty request then get message from SQS")
            booking_ids = self.get_sqs_message()
            if len(booking_ids) > 0:
                stmt = stmt.format(condition='BookingID IN (%s)')
                dfBooking = get_df_from_query(stmt=stmt, connection=booking_repository, list=booking_ids)

        if dfBooking is None or dfBooking.empty:
            print("[ETL BOOKING DASHBOARD] Booking not found.")
            return {
                'error': {
                    'code': '4004',
                    'message': 'Booking not found.'
                }
            }
        booking_ids = dfBooking['bookingid'].tolist()
        self.push_message_recheck_etl_booking(booking_ids, date_now)

        print("[ETL BOOKING DASHBOARD] query result bookingId list => ", booking_ids)
        print("[ETL BOOKING DASHBOARD] query result size df_booking => ", len(booking_ids))

        stmt = """SELECT bb.BookingBookerID, BookingID, BookerFullName, BookerEmail, BookerPhone, BookerUserID,
            MainBooker, BookerCompanyID, BookerInternalOrder, BookerCostCenterName, BookerCostCenterCode,
            BookerBusinessUnit, BookerBusinessArea, BookerBusinessPlace, BookerProfitCenter, BookerLevel,
            BookerDepartmentID, BookerDepartment, BookerPosition, CompanyNameEN, CompanyNameTH, BookerEmployeeID, BookerTaxCode, CompanyContribution,
            CompanySize, CompanyIndustryTypeSubName, RootCompanyNameEN, CompanyOwner
            FROM dbo.BookingBooker bb
            JOIN dbo.BookingBookerCompanyInformation bbc ON bb.BookingBookerID = bbc.BookingBookerID
            WHERE BookingID IN (%s)"""
        df_booking_booker = get_df_from_query(stmt=stmt, connection=booking_repository, list=booking_ids)
        print("[ETL BOOKING DASHBOARD] query result size df_booking_booker => ", len(df_booking_booker.index))

        stmt = """SELECT BookingSupplierID, BookingID, SupplierID, SupplierName, SupplierCurrency, SupplierExchangeRate,
            SupplierConfirmStatus, SupplierConfirmOTP, SupplierConfirmViewDate, SupplierConfirmUpdateDate,
            SupplierConfirmCode, SupplierConfirmNote, SupplierConfirmBy, SupplierEmail, SupplierTypeId, Vat,
            SapCode, SupplierCurrencyDataSource
            FROM dbo.BookingSupplier
            WHERE BookingID IN (%s)"""
        df_booking_supplier = get_df_from_query(stmt=stmt, connection=booking_repository, list=booking_ids)
        print("[ETL BOOKING DASHBOARD] query result size df_booking_supplier => ", len(df_booking_supplier.index))

        stmt = """SELECT BookingHotelID, BookingID, HotelID, HotelNameTH, HotelNameEN, AddressTH, AddressEN, Latitude,
            Longitude, DestinationID, DestinationNameTH, DestinationNameEN, AllotmentControl, HotelStar,
            HotelImageName, HotelContactNumber, CountryCodeISO2, CountryNameTH, CountryNameEN, HotelRemark, ContractingManagerOwner
            FROM dbo.BookingHotel
            WHERE BookingID IN (%s)"""
        df_booking_hotel = get_df_from_query(stmt=stmt, connection=booking_repository, list=booking_ids)
        print("[ETL BOOKING DASHBOARD] query result size df_booking_hotel => ", len(df_booking_hotel.index))

        stmt = """SELECT BookingPaymentID, BookingBookerID, PaymentType, TaxName, TaxID, TaxAddress, ChargeTotal, Vat,
            BookerCurrency, BookerExchangeRate, PayOnDate, BookerCompanyID, CancelChargeTotal
            FROM dbo.BookingPayment
            WHERE BookingBookerID IN (%s)"""
        df_booking_payment = get_df_from_query(stmt=stmt, connection=booking_repository,
                                               list=df_booking_booker['BookingBookerID'].tolist())
        print("[ETL BOOKING DASHBOARD] query result size df_booking_payment => ", len(df_booking_payment.index))

        stmt = """SELECT brt.BookingRoomTypeID, BookingSupplierID, RoomTypeID, RoomNameTH, RoomNameEN, BookingConditionID,
            ConditionOfferTypeName, ConditionNameTH, ConditionNameEN, BreakfastType, MaxAdult, MaxChild, Quantity,
            ConditionID, SellingPrice, Cost, RateKey, ExtraBed, Commission, OfferTypeDisplayTH, OfferTypeDisplayEN, OfferTypeRemark
            FROM dbo.BookingRoomType brt
            JOIN dbo.BookingCondition bc ON brt.BookingRoomTypeID = bc.BookingRoomTypeID
            WHERE brt.BookingSupplierID IN (%s)"""
        df_booking_roomtype_condition = get_df_from_query(stmt=stmt, connection=booking_repository,
                                                          list=df_booking_supplier['BookingSupplierID'].tolist())
        print(
            "[ETL BOOKING DASHBOARD] query result size df_booking_roomtype_condition => ",
            len(df_booking_roomtype_condition.index))

        stmt = """SELECT BookingGuestID, BookingID, BookingConditionID, Email, Nationality, SpecialRequest, RoomIndex, FullName
            FROM dbo.BookingGuest
            WHERE BookingID IN (%s)"""
        df_booking_guest = get_df_from_query(stmt=stmt, connection=booking_repository, list=booking_ids)
        print("[ETL BOOKING DASHBOARD] query result size df_booking_guest => ", len(df_booking_guest.index))

        stmt = """SELECT bpc.BookingCreditTermsID, BookingPaymentID,
            BillingName, BillingAddress, BillingPhone, CreditTermsDay, CompanySapCode, BookerBudget,
            BookerReporterEmail, BookingApprovalID, ApprovalEmail, ApprovalNameTH, ApprovalNameEN,
            ApprovalOTP, ApprovalStatus, ApprovalBy, ApprovalDate
            FROM dbo.BookingPaymentCreditTerms bpc
            LEFT JOIN dbo.BookingApproval ba ON bpc.BookingCreditTermsID = ba.BookingCreditTermsID
            WHERE BookingPaymentID IN (%s)"""
        df_booking_payment_credit_terms = get_df_from_query(stmt=stmt, connection=booking_repository,
                                                            list=df_booking_payment['BookingPaymentID'].tolist())
        print(
            "[ETL BOOKING DASHBOARD] query result size df_booking_payment_credit_terms => ",
            len(df_booking_payment_credit_terms.index))

        stmt = """SELECT BookingCreditTermsID, FieldID, Value
            FROM dbo.BookingCreditTermsCheckoutField bc
            WHERE BookingCreditTermsID IN (%s)"""
        df_booking_credit_terms_checkout_field = get_df_from_query(stmt=stmt, connection=booking_repository,
                                                            list=df_booking_payment_credit_terms['BookingCreditTermsID'].tolist())
        print(
            "[ETL BOOKING DASHBOARD] query result size df_booking_credit_terms_checkout_field => ",
            len(df_booking_credit_terms_checkout_field.index))

        stmt = "SELECT BookingCancelID, BookingID, BookingConditionCancellationID, ConditionID, RateKey, DayCancel, " \
               "CancellationID, ChargeBookerAmountTotal, ChargeBookerAmount, BookerExchangeRate, BookerCurrency, " \
               "SupplierChargeAmountTotal, SupplierChargeAmount, SupplierExchangeRate, SupplierCurrency, CreateDate, " \
               "Quantity, FromDate, BookingConditionID " \
               "FROM dbo.BookingCancel " \
               "WHERE BookingID IN (%s)"
        df_booking_cancel = get_df_from_query(stmt=stmt, connection=booking_repository, list=booking_ids)
        print("[ETL BOOKING DASHBOARD] query result size df_booking_cancel => ", len(df_booking_cancel.index))

        gbq_booking_booker_list = []
        gbq_booking_summary_list = []
        error_booking_id = []

        print("Pool size: ", POOL_SIZE)
        chunk_size = self.compute_chunksize(len(booking_ids))
        print("Chunk size: ", chunk_size)
        with ProcessPoolExecutor(max_workers=POOL_SIZE) as executor:
            result_list = executor.map(
                functools.partial(self.do_df_booking_dashboard, df_booking_booker, df_booking_supplier, df_booking_hotel, 
                                  df_booking_roomtype_condition, df_booking_guest, df_booking_cancel, 
                                  df_booking_payment, df_booking_payment_credit_terms, df_booking_credit_terms_checkout_field, date_now),
                dfBooking.iterrows(), chunksize=chunk_size)
            for result in result_list:
                gbq_booking_booker_list.extend(result.gbq_booking_booker_list)
                gbq_booking_summary_list.extend(result.gbq_booking_summary_list)
                error_booking_id.extend(result.error_booking_id)

        df_gbq_booking_booker = pd.DataFrame(data=gbq_booking_booker_list)
        df_gbq_booking_summary = pd.DataFrame(data=gbq_booking_summary_list)

        booking_ids = []
        if len(df_gbq_booking_booker) != 0:
            booking_ids = df_gbq_booking_booker['BookingID'].drop_duplicates().tolist()
            print('[ETL BOOKING DASHBOARD] BOOKER DATAFRAME Size => ', len(df_gbq_booking_booker))
            print('[ETL BOOKING DASHBOARD] SUMMARY DATAFRAME Size => ', len(df_gbq_booking_summary))

            futures = []
            futures.append(
                mp.Process(target=self.df_gbq_booking_booker_to_gbq,
                           args=(gbq_booking_booker_list,)))
            futures.append(
                mp.Process(target=self.df_gbq_booking_summary_to_gbq,
                           args=(gbq_booking_summary_list,)))
            futures.append(
                mp.Process(target=self.handler_product,
                           args=({'productIds': df_gbq_booking_booker['ProductID'].drop_duplicates().tolist()}, {})))
            futures.append(
                mp.Process(target=self.handler_destination,
                           args=({'destinationIds': df_gbq_booking_booker['DestinationID'].drop_duplicates().tolist()}, {})))
            futures.append(
                mp.Process(target=self.handler_supplier,
                           args=({'supplierIds': df_gbq_booking_booker['SupplierID'].drop_duplicates().tolist()}, {})))
            futures.append(
                mp.Process(target=self.handler_company_retention,
                           args=({'companyIds': df_gbq_booking_booker['CompanyID'].drop_duplicates().tolist()}, {})))
            futures.append(
                mp.Process(target=self.handler_booking_operation,
                           args=({'bookingIds': booking_ids},)))

            for future in futures:
                future.start()
            for future in futures:
                future.join()

        booking_repository.close()
        print('[ETL BOOKING DASHBOARD] Error booking id => ', error_booking_id)
        return {
            'booking_ids': booking_ids,
            'errorBookingIds': error_booking_id
        }

    def handler_booking_operation(self, event):
        print("[ETL BOOKING DASHBOARD][BOOKING OPERATION][REQUEST] => ", event)
        booking_ids = list(dict.fromkeys(event['bookingIds'])) if 'bookingIds' in event else None
        if booking_ids is not None and not all(isinstance(x, (int, numpy.int64)) for x in booking_ids):
            return {
                'error': {
                    'code': '4000',
                    'message': 'Invalid request'
                }
            }

        dateFrom = event['dateFrom'] if 'dateFrom' in event else None
        dateTo = event['dateTo'] if 'dateTo' in event else None
        dateType = event['dateType'] if 'dateType' in event else ETLDateType.booking_date
        df_booking_operation = None
        booking_operation_repository = pymssql.connect(
            user=self._config_client.get_attribute('database.user'),
            password=self._config_client.get_attribute('database.password'),
            database=self._config_client.get_attribute('database.booking-operation'),
            host=self._config_client.get_attribute('database.host')
        )

        stmt = """SELECT BookingCenter.BookingID, SupplierPayment, SupplierPaymentMethod, BookingInvoiceStatus, PayOutStatus, TaxInvoiceStatus, POD.PayOutDocumentID
            FROM booking_operation.dbo.BookingCenter
            OUTER APPLY (
                SELECT TOP 1 * FROM booking_operation.dbo.PayOutDocument POD 
            	WHERE BookingCenter.BookingID = POD.BookingID
            	AND POD.Status=1
            ) POD
            WHERE BookingCenter.BookingStatus IN ('success', 'cancel_credit', 'cancel_non_credit', 'cancel_booking', 'cancel_multi_payment')
            AND {condition}
            """
        if dateFrom is not None and dateTo is not None:
            if dateType == ETLDateType.booking_date:
                stmt = stmt.format(condition="""BookingCenter.BookingDate BETWEEN '{0}' AND '{1}'""".format(dateFrom, dateTo))
            elif dateType == ETLDateType.checkin_date:
                stmt = stmt.format(condition="""BookingCenter.CheckIn BETWEEN '{0}' AND '{1}'""".format(dateFrom, dateTo))
            
            df_booking_operation = pd.read_sql_query(stmt, booking_operation_repository)
        
        elif booking_ids is not None:
            stmt = stmt.format(condition='BookingCenter.BookingID IN (%s)')
            df_booking_operation = get_df_from_query(stmt=stmt, connection=booking_operation_repository, list=booking_ids)
        
        else:
            print("[ETL BOOKING DASHBOARD][BOOKING OPERATION] Empty request then get message from SQS")
            booking_ids = self.get_sqs_message(self._queue_url_etl_booking_operation)
            if len(booking_ids) > 0:
                stmt = stmt.format(condition='BookingCenter.BookingID IN (%s)')
                df_booking_operation = get_df_from_query(stmt=stmt, connection=booking_operation_repository, list=booking_ids)

        print("[ETL BOOKING DASHBOARD][BOOKING OPERATION] query result size df_booking_operation => ", len(df_booking_operation.index))
        if df_booking_operation is None or df_booking_operation.empty:
            print("[ETL BOOKING DASHBOARD][BOOKING OPERATION] Booking not found.")
            return {
                'error': {
                    'code': '4004',
                    'message': 'Booking not found.'
                }
            }

        gbq_booking_operation_list = []
        booking_operation_ids = []
        
        for i, item in df_booking_operation.iterrows():
            booking_operation_ids.append(item.BookingID)
            gbq_booking_operation_list.append({
                'BookingID': item.BookingID,
                'SupplierPaidBy': Models.switcher_supplier_paid_by.get('{}_{}'.format(item.SupplierPayment, item.SupplierPaymentMethod), None),
                'InvoiceStatus': Models.switcher_invoice_status.get(item.BookingInvoiceStatus, None),
                'PayoutStatus': Models.switcher_pay_out_status.get(item.PayOutStatus, None),
                'PaySlipStatus': None if pd.isna(item.PayOutStatus) else 'Pending' if pd.isna(item.PayOutDocumentID) else 'Attached',
                'TaxInvoiceStatus': Models.switcher_tax_invoice_status.get(item.TaxInvoiceStatus, None)
            })
        self.df_gbq_booking_operation_to_gbq(gbq_booking_operation_list)
        booking_operation_repository.close()
        return {'booking_ids': booking_operation_ids}


class ETLBookingValidationService:
    def __init__(self, config_client) -> None:
        self._config_client = config_client
        self._aws_access_key_id = config_client.get_attribute('aws.access.key.id')
        self._aws_secret_access_key = config_client.get_attribute('aws.secret.access.key')
        self._region_name = config_client.get_attribute('aws.region')
        self._queue_url_etl_booking = config_client.get_attribute('sqs.queue.url.etl-booking')
        self._queue_url_etl_booking_validation = config_client.get_attribute('sqs.queue.url.etl-booking-validation')
        google_key = self._config_client.get_attribute('google.key')
        service_account_info = json.loads(google_key, strict=False)
        credentials = service_account.Credentials.from_service_account_info(
            service_account_info)
        self._credentials = credentials
        self._gbq_group_table = config_client.get_attribute('google.bq.group.table')
        self._gbq_booking_summary_table = config_client.get_attribute('google.bq.table.booking-summary')

    def get_sqs_message(self):
        sqs = boto3.client('sqs',
                           region_name=self._region_name,
                           aws_access_key_id=self._aws_access_key_id,
                           aws_secret_access_key=self._aws_secret_access_key)

        bookings = []

        while True:
            messages = sqs.receive_message(QueueUrl=self._queue_url_etl_booking_validation, MaxNumberOfMessages=10)
            if 'Messages' in messages:
                for message in messages['Messages']:
                    if message['Body'] not in bookings:
                        try:
                            bookings.append(json.loads(message['Body']))
                        except Exception as e:
                            print('[ETL BOOKING DASHBOARD VALIDATION] Exception with : ', e)
                    sqs.delete_message(QueueUrl=self._queue_url_etl_booking_validation,
                                       ReceiptHandle=message['ReceiptHandle'])
            else:
                print('[ETL BOOKING DASHBOARD VALIDATION] Queue is now empty')
                break
        df_booking_list = []
        for row in bookings:
            df_booking_list.append(pd.DataFrame(
                {
                    "bookingId": row['bookingIds'],
                    "timestamp": numpy.array([row['timestamp']] * len(row['bookingIds']), dtype="str")
                }
            ))
        if len(df_booking_list) == 0:
            return None
        df_booking = pd.concat(df_booking_list).sort_values(by=['bookingId', 'timestamp'])
        df_booking = df_booking.drop_duplicates(subset=['bookingId'], keep='last')
        return df_booking

    def push_message_etl_booking(self, booking_ids):
        sqs = boto3.client('sqs',
                           region_name=self._region_name,
                           aws_access_key_id=self._aws_access_key_id,
                           aws_secret_access_key=self._aws_secret_access_key)
        maxBatchSize = 10  # current maximum allowed
        chunks = [booking_ids[x:x + maxBatchSize] for x in range(0, len(booking_ids), maxBatchSize)]
        for chunk in chunks:
            entries = []
            for x in chunk:
                entry = {'Id': str(x),
                         'MessageBody': str(x),
                         'MessageGroupId': 'etl-booking'}
                entries.append(entry)
            response = sqs.send_message_batch(QueueUrl=self._queue_url_etl_booking,
                                              Entries=entries, )
            print(response)

    def handler(self):
        bookings = self.get_sqs_message()
        print('[ETL BOOKING DASHBOARD VALIDATION] => ', bookings)
        if bookings is None:
            return []
        n = 500
        chunks = [bookings[i:i + n] for i in range(0, bookings.shape[0], n)]
        booking_result_list = []
        gbq_client = bigquery.Client(
            credentials=self._credentials,
            project=self._credentials.project_id,
        )
        for row in chunks:
            sql = """SELECT bookingId,updatedDate 
                FROM `{}`
                WHERE BookingID in ({})""".format(
                '%s.%s' % (self._gbq_group_table, self._gbq_booking_summary_table), 
                ','.join(map(str, row['bookingId'].tolist())))
            
            df = gbq_client.query(sql).to_dataframe()
            booking_result_list.append(df)
        result = pd.concat(booking_result_list)
        result['updatedDate'] = pd.to_datetime(result['updatedDate'], errors='coerce')
        result['updatedDate'] = result['updatedDate'].dt.strftime('%Y-%m-%dT%H:%M:%S.%f')
        bookings = bookings.astype({'bookingId': 'int64'})
        result = result.astype({'bookingId': 'int64'})
        result = pd.merge(bookings, result, how="left", on=["bookingId"])
        comparison_column = numpy.where(result["timestamp"] == result["updatedDate"], True, False)
        result["equal"] = comparison_column
        print('[ETL BOOKING DASHBOARD VALIDATION][comparison] => ', result)
        abnormal_booking = result[result['equal'] == False]
        abnormal_booking_ids = abnormal_booking['bookingId'].tolist()
        if len(abnormal_booking_ids) > 0:
            self.push_message_etl_booking(abnormal_booking['bookingId'].tolist())
        return abnormal_booking_ids
