
import functools
import json
import multiprocessing as mp
from multiprocessing import connection
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

from app.utils import GbqBookingBooker, GbqBookingGuest, GbqBookingRating, GbqBookingRoomtype, GbqBookingSupplier, GbqBookingCondition, GbqHotelSupplier, round_half_up, Models, days_between, compare, \
     GbqBooking, GbqBookingHotel, get_df_from_query
from app.models.etl_date_type_request import ETLDateType

POOL_SIZE = mp.cpu_count()

class SupplierDashboardService:

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
        self._gbq_group_table = 'alpha_supplier_dashboard_source'
        self._gbq_booking_table = 'Booking'
        self._gbq_booking_booker_table = 'BookingBooker'
        self._gbq_booking_hotel_table = 'BookingHotel'
        self._gbq_booking_supplier_table = 'BookingSupplier'
        self._gbq_booking_roomtype_table = 'BookingRoomType'
        self._gbq_hotel_supplier_table = 'HotelSupplier'
        self._gbq_booking_condition_table = 'BookingCondition'
        self._gbq_booking_guest_table = 'BookingGuest'
        self._gbq_booking_rating_table = 'Rating'


    def do_df_booking_dashboard():
        print("Hi")

    def handler_booking(self, event, context):

        event = pd.DataFrame(data=event)
        if event.empty :
            return {
                'error': {
                    'code': '4004',
                    'message': 'Booking not found'
                }
            }
        gbq_booking_list = []
        for i,val in event.iterrows():
            gbq_booking_tmp = GbqBooking()
            gbq_booking_tmp.BookingID = val['bookingid']
            gbq_booking_tmp.BookingStatus = Models.switcher_booking_status.get(val['bookingstatus'], None)
            gbq_booking_tmp.BookingDate = val['bookingdate']
            gbq_booking_tmp.CancelDate = val['canceldate']
            gbq_booking_tmp.UpdateBy = val['updatedby']
            gbq_booking_tmp.UpdateDate = val['updateddate']
            gbq_booking_tmp.Purpose_Temp = Models.switcher_purpose.get(val['purpose'], None)
            gbq_booking_tmp.BookingCheckInDate = val['checkin'].date()
            gbq_booking_tmp.BookingCheckOutDate = val['checkout'].date()
            gbq_booking_tmp.LengthOfStay = val['LengthOfStay']
            if(val['PayOutStatus'] != None and val['PayOutStatus'] != 'unpaid'):
                gbq_booking_tmp.PaidingStatus = Models.switcher_paiding_status.get(val['PayOutStatus'][0:4], None)
            else:
                gbq_booking_tmp.PaidingStatus = Models.switcher_paiding_status.get(val['PayOutStatus'], None)
            gbq_booking_tmp.InvoiceStatus = Models.switcher_invoice_status.get(val['InvoiceStatus'], None)
            gbq_booking_tmp.TaxInvoiceStatus = Models.switcher_tax_invoice_status.get(val['TaxInvoiceStatus'], None)
            gbq_booking_list.append(gbq_booking_tmp.__dict__)
        df_gbq_booking = pd.DataFrame(data=gbq_booking_list, columns=gbq_booking_list[0].keys())
        print("Handler_Booking: ", df_gbq_booking)
        try:
            QUERY = ('DELETE FROM ascend-travel-prod.{0}.{1} WHERE BookingID IN ({2})'.format(
                    self._gbq_group_table,
                    self._gbq_booking_table,
                    ','.join(map(str, df_gbq_booking['BookingID'].tolist()))))

            gbq_client = bigquery.Client(
                credentials=self._credentials,
                project=self._credentials.project_id,
            )
            query_job = gbq_client.query(QUERY)  # API request
            query_job.result()
        except Exception as e:
            print('[ETL BOOKING DASHBOARD][Booking] Exception with : ', e)
        df_gbq_booking = df_gbq_booking.astype({'BookingID': 'int64'})
        df_gbq_booking.to_gbq(destination_table='%s.%s' % (self._gbq_group_table, self._gbq_booking_table),
                                if_exists='append',
                                project_id=self._credentials.project_id, credentials=self._credentials,
                                table_schema = Models.booking_schema)


    def handler_booking_booker(self, event, context):

        event = pd.DataFrame(data=event)
        if event.empty :
            return {
                'error': {
                    'code': '4004',
                    'message': 'Booking Booker not found'
                }
            }
        gbq_booking_booker_list = []
        for i,val in event.iterrows():
            gbq_booker_tmp = GbqBookingBooker()
            gbq_booker_tmp.BookingBookerID = val['BookingBookerID']
            gbq_booker_tmp.BookingID = val['BookingID']
            gbq_booker_tmp.BookerFullName = val['BookerFullName']
            gbq_booker_tmp.BookerEmail = val['BookerEmail']
            gbq_booker_tmp.BookerUserID = val['BookerUserID']
            gbq_booking_booker_list.append(gbq_booker_tmp.__dict__)
        df_gbq_booker = pd.DataFrame(data=gbq_booking_booker_list, columns=gbq_booking_booker_list[0].keys())
        print("Handler_Booker: ", df_gbq_booker)
        try:
            QUERY = ('DELETE FROM ascend-travel-prod.{0}.{1} WHERE BookingID IN ({2})'.format(
                    self._gbq_group_table,
                    self._gbq_booking_booker_table,
                    ','.join(map(str, df_gbq_booker['BookingID'].tolist()))))

            gbq_client = bigquery.Client(
                credentials=self._credentials,
                project=self._credentials.project_id,
            )
            query_job = gbq_client.query(QUERY)  # API request
            query_job.result()
        except Exception as e:
            print('[ETL BOOKING DASHBOARD][BookingBooker] Exception with : ', e)
        df_gbq_booker = df_gbq_booker.astype({'BookingID': 'int64'})
        df_gbq_booker.to_gbq(destination_table='%s.%s' % (self._gbq_group_table, self._gbq_booking_booker_table),
                                if_exists='append',
                                project_id=self._credentials.project_id, credentials=self._credentials)


    def handler_booking_guest(self, event, context):
        event = pd.DataFrame(data=event)
        if event.empty :
            return {
                'error': {
                    'code': '4004',
                    'message': 'Booking Guest not found'
                }
            }
        gbq_booking_guest_list = []
        for i,val in event.iterrows():
            gbq_guest_tmp = GbqBookingGuest()
            gbq_guest_tmp.BookingGuestID = val['BookingGuestID']
            gbq_guest_tmp.BookingID = val['BookingID']
            gbq_guest_tmp.BookingConditionID = val['BookingConditionID']
            gbq_guest_tmp.FullName = val['FullName']
            gbq_booking_guest_list.append(gbq_guest_tmp.__dict__)
        df_gbq_guest = pd.DataFrame(data=gbq_booking_guest_list, columns=gbq_booking_guest_list[0].keys())
        print("Handler_Guest: ", df_gbq_guest)
        try:
            QUERY = ('DELETE FROM ascend-travel-prod.{0}.{1} WHERE BookingID IN ({2})'.format(
                    self._gbq_group_table,
                    self._gbq_booking_guest_table,
                    ','.join(map(str, df_gbq_guest['BookingID'].tolist()))))

            gbq_client = bigquery.Client(
                credentials=self._credentials,
                project=self._credentials.project_id,
            )
            query_job = gbq_client.query(QUERY)  # API request
            query_job.result()
        except Exception as e:
            print('[ETL BOOKING DASHBOARD][BookingBooker] Exception with : ', e)
        df_gbq_guest = df_gbq_guest.astype({'BookingID': 'int64'})
        df_gbq_guest.to_gbq(destination_table='%s.%s' % (self._gbq_group_table, self._gbq_booking_guest_table),
                                if_exists='append',
                                project_id=self._credentials.project_id, credentials=self._credentials)




    def handler_booking_hotel(self, event, context):

        event = pd.DataFrame(data=event)
        if event.empty :
            return {
                'error': {
                    'code': '4004',
                    'message': 'Booking Hotel not found'
                }
            }
        gbq_booking_hotel_list = []
        for i,val in event.iterrows():
            gbq_hotel_tmp = GbqBookingHotel()
            gbq_hotel_tmp.BookingHotelID = val['BookingHotelID']
            gbq_hotel_tmp.BookingID = val['BookingID']
            gbq_hotel_tmp.HotelID = val['HotelID']
            gbq_hotel_tmp.HotelNameTH = val['HotelNameTH']
            gbq_hotel_tmp.HotelNameEN = val['HotelNameEN']
            gbq_hotel_tmp.DestinationID = val['DestinationID']
            gbq_hotel_tmp.DestinationNameTH = val['DestinationNameTH']
            gbq_hotel_tmp.DestinationEN = val['DestinationNameEN']
            gbq_booking_hotel_list.append(gbq_hotel_tmp.__dict__)
        df_gbq_booking = pd.DataFrame(data=gbq_booking_hotel_list, columns=gbq_booking_hotel_list[0].keys())
        print("Handler_Booking_Hotel: ", df_gbq_booking)
        try:
            QUERY = ('DELETE FROM ascend-travel-prod.{0}.{1} WHERE BookingID IN ({2})'.format(
                    self._gbq_group_table,
                    self._gbq_booking_hotel_table,
                    ','.join(map(str, df_gbq_booking['BookingID'].tolist()))))

            gbq_client = bigquery.Client(
                credentials=self._credentials,
                project=self._credentials.project_id,
            )
            query_job = gbq_client.query(QUERY)  # API request
            query_job.result()
        except Exception as e:
            print('[ETL BOOKING DASHBOARD][Hotel] Exception with : ', e)
        df_gbq_booking.to_gbq(destination_table='%s.%s' % (self._gbq_group_table, self._gbq_booking_hotel_table),
                                if_exists='append',
                                project_id=self._credentials.project_id, credentials=self._credentials)


    def handler_booking_supplier(self, event, context):
        if event.empty :
            return {
                'error': {
                    'code': '4004',
                    'message': 'Booking Supplier not found'
                }
            }
        event = pd.DataFrame(data=event)
        gbq_booking_supplier_list = []
        for i,val in event.iterrows():
            gbq_supplier_tmp = GbqBookingSupplier()
            gbq_supplier_tmp.BookingSupplierID = val['BookingSupplierID']
            gbq_supplier_tmp.BookingID = val['BookingID']
            gbq_supplier_tmp.SupplierID = val['SupplierID']
            gbq_supplier_tmp.SupplierTypeID = val['SupplierTypeId']
            gbq_supplier_tmp.SupplierName = val['SupplierName']
            gbq_supplier_tmp.SupplierConfirmStatus = val['SupplierConfirmStatus']
            gbq_supplier_tmp.SupplierConfirmDate = val['SupplierConfirmDate']
            gbq_booking_supplier_list.append(gbq_supplier_tmp.__dict__)
        df_gbq_supplier = pd.DataFrame(data=gbq_booking_supplier_list, columns=gbq_booking_supplier_list[0].keys())
        print("Handler_Booking_Supplier: ", df_gbq_supplier)
        try:
            QUERY = ('DELETE FROM ascend-travel-prod.{0}.{1} WHERE BookingID IN ({2})'.format(
                    self._gbq_group_table,
                    self._gbq_booking_supplier_table,
                    ','.join(map(str, df_gbq_supplier['BookingID'].tolist()))))

            gbq_client = bigquery.Client(
                credentials=self._credentials,
                project=self._credentials.project_id,
            )
            query_job = gbq_client.query(QUERY)  # API request
            query_job.result()
        except Exception as e:
            print('[ETL BOOKING DASHBOARD][Supplier] Exception with : ', e)
        df_gbq_supplier.to_gbq(destination_table='%s.%s' % (self._gbq_group_table, self._gbq_booking_supplier_table),
                                if_exists='append',
                                project_id=self._credentials.project_id, credentials=self._credentials,
                                table_schema=Models.booking_supplier_schema)

    def handler_hotel_supplier(self, event, context):
        if event.empty :
            return {
                'error': {
                    'code': '4004',
                    'message': 'Booking Supplier not found'
                }
            }
        event = pd.DataFrame(data=event)
        gbq_hotel_supplier_list = []
        for i,val in event.iterrows():
            gbq_supplier_tmp = GbqHotelSupplier()
            gbq_supplier_tmp.HotelSupplierID = val['HotelSupplierID']
            gbq_supplier_tmp.SupplierID = val['SupplierID']
            gbq_supplier_tmp.HotelID = val['HotelID']
            gbq_hotel_supplier_list.append(gbq_supplier_tmp.__dict__)
        df_gbq_supplier = pd.DataFrame(data=gbq_hotel_supplier_list, columns=gbq_hotel_supplier_list[0].keys())
        print("Handler_Hotel_Supplier: ", df_gbq_supplier)
        try:
            QUERY = ('DELETE FROM ascend-travel-prod.{0}.{1} WHERE HotelSupplierID IN ({2})'.format(
                    self._gbq_group_table,
                    self._gbq_hotel_supplier_table,
                    ','.join(map(str, df_gbq_supplier['HotelSupplierID'].tolist()))))

            gbq_client = bigquery.Client(
                credentials=self._credentials,
                project=self._credentials.project_id,
            )
            query_job = gbq_client.query(QUERY)  # API request
            query_job.result()
        except Exception as e:
            print('[ETL BOOKING DASHBOARD][HotelSupplier] Exception with : ', e)
        df_gbq_supplier.to_gbq(destination_table='%s.%s' % (self._gbq_group_table, self._gbq_hotel_supplier_table),
                                if_exists='append',
                                project_id=self._credentials.project_id, credentials=self._credentials)




    def handler_booking_roomtype(self, event, context):
        event = pd.DataFrame(data=event)
        if event.empty :
            return {
                'error': {
                    'code': '4004',
                    'message': 'Booking Roomtype not found'
                }
            }
        gbq_booking_roomtype_list = []
        for i,val in event.iterrows():
            gbq_rommtype_tmp = GbqBookingRoomtype()
            gbq_rommtype_tmp.BookingRoomTypeID = val['BookingRoomtypeID']
            gbq_rommtype_tmp.BookingSupplierID = val['BookingSupplierID']
            gbq_rommtype_tmp.RoomTypeID = val['RoomTypeID']
            gbq_rommtype_tmp.RoomTypeNameTH = val['RoomNameTH']
            gbq_rommtype_tmp.RoomTypeNameEN = val['RoomNameEN']
            gbq_booking_roomtype_list.append(gbq_rommtype_tmp.__dict__)
        df_gbq_roomtype = pd.DataFrame(data=gbq_booking_roomtype_list, columns=gbq_booking_roomtype_list[0].keys())
        print("Handler_Booking_Roomtype: ", df_gbq_roomtype)
        try:
            QUERY = ('DELETE FROM ascend-travel-prod.{0}.{1} WHERE BookingSupplierID IN ({2})'.format(
                    self._gbq_group_table,
                    self._gbq_booking_roomtype_table,
                    ','.join(map(str, df_gbq_roomtype['BookingSupplierID'].tolist()))))

            gbq_client = bigquery.Client(
                credentials=self._credentials,
                project=self._credentials.project_id,
            )
            query_job = gbq_client.query(QUERY)  # API request
            query_job.result()
        except Exception as e:
            print('[ETL BOOKING DASHBOARD][Roomtype] Exception with : ', e)
        df_gbq_roomtype.to_gbq(destination_table='%s.%s' % (self._gbq_group_table, self._gbq_booking_roomtype_table),
                                if_exists='append',
                                project_id=self._credentials.project_id, credentials=self._credentials)


    def handler_booking_condition(self, event, context):
        event = pd.DataFrame(data=event)
        if event.empty :
            return {
                'error': {
                    'code': '4004',
                    'message': 'Booking Condition not found'
                }
            }
        gbq_booking_condition_list = []
        for i,val in event.iterrows():
            gbq_condition_tmp = GbqBookingCondition()
            gbq_condition_tmp.BookingConditionID = val['BookingConditionID']
            gbq_condition_tmp.BookingRoomTypeID = val['BookingRoomTypeID']
            gbq_condition_tmp.ConditionID = val['ConditionID']
            gbq_condition_tmp.ConditionOfferTypeName = val['ConditionOfferTypeName']
            gbq_condition_tmp.Quantity = val['Quantity']
            gbq_condition_tmp.SellingPrice = val['SellingPrice']
            gbq_condition_tmp.Cost = val['Cost']
            gbq_booking_condition_list.append(gbq_condition_tmp.__dict__)
        df_gbq_condition = pd.DataFrame(data=gbq_booking_condition_list, columns=gbq_booking_condition_list[0].keys())
        print("Handler_Booking_Condition: ", df_gbq_condition)
        try:
            QUERY = ('DELETE FROM ascend-travel-prod.{0}.{1} WHERE BookingRoomTypeID IN ({2})'.format(
                    self._gbq_group_table,
                    self._gbq_booking_condition_table,
                    ','.join(map(str, df_gbq_condition['BookingRoomTypeID'].tolist()))))

            gbq_client = bigquery.Client(
                credentials=self._credentials,
                project=self._credentials.project_id,
            )
            query_job = gbq_client.query(QUERY)  # API request
            query_job.result()
        except Exception as e:
            print('[ETL BOOKING DASHBOARD][Condition] Exception with : ', e)
        df_gbq_condition.to_gbq(destination_table='%s.%s' % (self._gbq_group_table, self._gbq_booking_condition_table),
                                if_exists='append',
                                project_id=self._credentials.project_id, credentials=self._credentials)


    def handler_review_rating(self, event, context):
        event = pd.DataFrame(data=event)
        if event.empty :
            return {
                'error': {
                    'code': '4004',
                    'message': 'Review Rating not found'
                }
            }
        gbq_booking_rating_list = []
        for i,val in event.iterrows():
            gbq_condition_tmp = GbqBookingRating()
            gbq_condition_tmp.RatingID = val['RatingID']
            gbq_condition_tmp.BookingID = val['BookingID']
            gbq_condition_tmp.TravellerName= val['Name']
            gbq_condition_tmp.HotelNameEN = val['ProductNameEN']
            gbq_condition_tmp.ValueForMoney = val['ValueForMoney']
            gbq_condition_tmp.Location = val['Location']
            gbq_condition_tmp.ServiceAndStaff = val['Service/Staff']
            gbq_condition_tmp.Facilities = val['Facilities']
            gbq_condition_tmp.Cleanliness = val['Cleanliness']
            gbq_booking_rating_list.append(gbq_condition_tmp.__dict__)
        df_gbq_rating = pd.DataFrame(data=gbq_booking_rating_list, columns=gbq_booking_rating_list[0].keys())
        print("Handler_Booking_Rating: ", df_gbq_rating)
        try:
            QUERY = ('DELETE FROM ascend-travel-prod.{0}.{1} WHERE BookingID IN ({2})'.format(
                    self._gbq_group_table,
                    self._gbq_booking_rating_table,
                    ','.join(map(str, df_gbq_rating['BookingID'].tolist()))))

            gbq_client = bigquery.Client(
                credentials=self._credentials,
                project=self._credentials.project_id,
            )
            query_job = gbq_client.query(QUERY)  # API request
            query_job.result()
        except Exception as e:
            print('[ETL BOOKING DASHBOARD][Condition] Exception with : ', e)
        df_gbq_rating.to_gbq(destination_table='%s.%s' % (self._gbq_group_table, self._gbq_booking_rating_table),
                                if_exists='append',
                                project_id=self._credentials.project_id, credentials=self._credentials)



    def compute_chunksize(self, iterable_size):
        if iterable_size == 0:
            return 0
        chunksize, extra = divmod(iterable_size, POOL_SIZE * 4)
        if extra:
            chunksize += 1
        return chunksize

    def handler_Test(self, event): # event = body request
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
        
        # setting database booking
        host = self._config_client.get_attribute('database.host')
        user = self._config_client.get_attribute('database.user')
        password = self._config_client.get_attribute('database.password')
        booking_db = self._config_client.get_attribute('database.booking')

        #connect database booking
        booking_repository = pymssql.connect(
            user=user,
            password=password,
            database=booking_db,
            host=host
        )

        #connect database booking operation
        booking_operation_repository = pymssql.connect(
            user=self._config_client.get_attribute('database.user'),
            password=self._config_client.get_attribute('database.password'),
            database=self._config_client.get_attribute('database.booking-operation'),
            host=self._config_client.get_attribute('database.host')
        )

        #connect database booking operation
        act_repository = pymssql.connect(
            user=self._config_client.get_attribute('database.user'),
            password=self._config_client.get_attribute('database.password'),
            database=self._config_client.get_attribute('database.act'),
            host=self._config_client.get_attribute('database.host')
        )

        #connect database review
        rating_repository = pymssql.connect(
            user=self._config_client.get_attribute('database.user'),
            password=self._config_client.get_attribute('database.password'),
            database='review',
            host=self._config_client.get_attribute('database.host')
        )

        stmt = """SELECT bookingid, bookingstatus, bookingdate, canceldate, purpose, checkin, checkout, DAY(checkout) - DAY(checkin) AS LengthOfStay, updatedby, updateddate
                    FROM dbo.Booking
                    WHERE {condition} """
        if dateFrom is not None and dateTo is not None:
            if dateType == ETLDateType.booking_date:
                stmt = stmt.format(condition="""BookingDate BETWEEN '{0}' AND '{1}'""".format(dateFrom, dateTo))
            elif dateType == ETLDateType.checkin_date:
                stmt = stmt.format(condition="""CheckIn BETWEEN '{0}' AND '{1}'""".format(dateFrom, dateTo))
            dfBooking = pd.read_sql_query(stmt, booking_repository)
        
        elif booking_ids is not None:
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
        print(dfBooking)

        stmt = """SELECT BookingCenter.BookingID as bookingid, SupplierPayment, SupplierPaymentMethod, BookingInvoiceStatus AS InvoiceStatus, PayOutStatus, TaxInvoiceStatus
            FROM booking_operation.dbo.BookingCenter
            WHERE BookingCenter.BookingID IN (%s)"""
        df_booking_payment = get_df_from_query(stmt=stmt, connection=booking_operation_repository, list=booking_ids)
        # print(df_booking_payment)

        # merge dataframe booking with bookingcenter
        merge_booking = pd.merge(dfBooking, df_booking_payment, on = "bookingid")
        # print(pd.merge(dfBooking, df_booking_payment, on = "bookingid"))


        stmt = """SELECT BookingBookerID, BookingID, BookerFullName, BookerEmail, BookerUserID
               FROM dbo.BookingBooker
               WHERE BookingID IN (%s) """
        df_booking_booker = get_df_from_query(stmt=stmt, connection=booking_repository, list=booking_ids)
        # print(df_booking_booker)

        stmt = """SELECT BookingGuestID, BookingID, BookingConditionID, FullName
               FROM dbo.BookingGuest
               WHERE BookingID IN (%s) """
        df_booking_guest = get_df_from_query(stmt=stmt, connection=booking_repository, list=booking_ids)
        # print(df_booking_guest)

        stmt = """SELECT BookingHotelID, BookingID, HotelID, HotelNameTH, HotelNameEN, DestinationID,
            DestinationNameTH, DestinationNameEN, CheckInTime, CheckOutTime
            FROM dbo.BookingHotel 
            WHERE BookingID IN (%s) """
        df_booking_hotel = get_df_from_query(stmt=stmt, connection=booking_repository, list=booking_ids)
        hotelID_list = df_booking_hotel['HotelID'].tolist()
        # print(df_booking_hotel)

        stmt = """SELECT BookingSupplierID, BookingID, SupplierID, SupplierTypeId, SupplierName, SupplierConfirmStatus, SupplierConfirmUpdateDate AS SupplierConfirmDate
            FROM dbo.BookingSupplier
            WHERE BookingID IN (%s) """
        df_booking_supplier = get_df_from_query(stmt=stmt, connection=booking_repository, list=booking_ids)
        # print(df_booking_supplier)
        booking_supplierID = df_booking_supplier['BookingSupplierID'].tolist()

        stmt = """SELECT HotelSupplierID, SupplierID, HotelID
                  FROM dbo.HotelSupplier
                  WHERE HotelID IN (%s) """
        df_hotel_supplier = get_df_from_query(stmt=stmt, connection=act_repository, list=hotelID_list)
        print(df_hotel_supplier)
        

        stmt = """SELECT BookingRoomtypeID, BookingSupplierID, RoomTypeID, RoomNameTH, RoomNameEN
            FROM dbo.BookingRoomtype
            WHERE BookingSupplierID IN (%s) """
        df_booking_roomtype = get_df_from_query(stmt=stmt, connection=booking_repository, list=booking_supplierID)
        # print(df_booking_roomtype)
        booking_roomtypeID = df_booking_roomtype['BookingRoomtypeID'].tolist()

        stmt = """SELECT BookingConditionID, BookingRoomTypeID, ConditionID, ConditionOfferTypeName, Quantity, SellingPrice, Cost
            FROM dbo.BookingCondition
            WHERE BookingRoomTypeID IN (%s) """
        df_booking_condition = get_df_from_query(stmt=stmt, connection=booking_repository, list=booking_roomtypeID)
        # print(df_booking_condition)

        stmt = """SELECT 
                    *,
	                [Value for Money] AS ValueForMoney FROM 
                    (
                    SELECT r.RatingID, r.BookingID, r.Name, r.ProductNameEN, SUM(rs.Score) AS Score, rt.TitleEN 
                    FROM review.dbo.Rating r
                    INNER JOIN review.dbo.RatingScore rs 
                    ON r.RatingID = rs.RatingID
                    INNER JOIN review.dbo.RatingTitle rt 
                    ON rs.RatingTitleID = rt.RatingTitleID
                    WHERE BookingID IN (%s)
                    GROUP BY 
                        r.RatingID,
                        r.BookingID,
                        r.Name,
                        r.ProductNameEN,
                        rt.TitleEN
                    ) t
                    PIVOT (
                        Max(Score) 
                        FOR TitleEN IN ([Service/Staff], [Cleanliness], [Location], [Facilities], [Value for Money])
                    ) pivot_table
                    ORDER BY RatingID
                """
        # stmt = """SELECT * FROM review.dbo.Rating WHERE BookingID IN (%s)"""
        # example of connect direct to database that want to query
        df_booking_rating = get_df_from_query(stmt=stmt, connection=rating_repository, list=booking_ids)
        print(booking_ids)
        print("Rating: ", df_booking_rating)



        gbq_booking_booker_list = []
        gbq_booking_summary_list = []
        error_booking_id = []

        print("Pool size: ", POOL_SIZE)
        chunk_size = self.compute_chunksize(len(booking_ids))
        print("Chunk size: ", chunk_size)

        # with ProcessPoolExecutor(max_workers=POOL_SIZE) as executor:
        #     result_list = executor.map(
        #         functools.partial(self.do_df_booking_dashboard, ), dfBooking.iterrows(), chunksize=chunk_size)
        #     for result in result_list:
        #         gbq_booking_booker_list.extend(result.gbq_booking_booker_list)
        #         gbq_booking_summary_list.extend(result.gbq_booking_summary_list)
        #         error_booking_id.extend(result.error_booking_id)

        # df_gbq_booking_booker = pd.DataFrame(data=gbq_booking_booker_list)
        # df_gbq_booking_summary = pd.DataFrame(data=gbq_booking_summary_list)
        
        print("Here")
        futures = []
        futures.append(
                mp.Process(target=self.handler_booking,
                           args=(merge_booking, {})))

        futures.append(
                mp.Process(target=self.handler_hotel_supplier,
                           args=(df_hotel_supplier, {})))

        futures.append(
                mp.Process(target=self.handler_booking_booker,
                           args=(df_booking_booker, {})))

        futures.append(
                mp.Process(target=self.handler_booking_guest,
                           args=(df_booking_guest, {})))

        futures.append(
                mp.Process(target=self.handler_booking_hotel,
                           args=(df_booking_hotel, {})))

        futures.append(
                mp.Process(target=self.handler_booking_supplier,
                           args=(df_booking_supplier, {})))

        futures.append(
                mp.Process(target=self.handler_booking_roomtype,
                           args=(df_booking_roomtype, {})))

        futures.append(
                mp.Process(target=self.handler_booking_condition,
                           args=(df_booking_condition, {})))

        futures.append(
                mp.Process(target=self.handler_review_rating,
                           args=(df_booking_rating, {})))

        

        for future in futures:
            future.start()
        for future in futures:
            future.join()

        print("Here1")

        booking_repository.close()
        booking_operation_repository.close()
        act_repository.close()
        rating_repository.close()

        return {
            'booking_ids': booking_ids,
            'errorBookingIds': error_booking_id
        }

    