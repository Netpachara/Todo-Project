import math
from datetime import datetime

import pandas as pd


class Models:
    switcher_booking_status = {
        'success': "Success",
        'fail': "Fail",
        'cancel_credit': "Cancel credit",
        'cancel_non_credit': "Cancel non credit",
        'cancel_booking': "Cancel booking",
        'cancel_multi_payment': "Cancel Multi-payment"
    }
    switcher_contribution = {
        'ascend': "Ascend",
        'external': "External",
        'cp': "CP"
    }

    switcher_company_size = {
        'large': "Large",
        'medium': "Medium",
        'small': "Small"
    }

    switcher_paiding_status = {
        'paid': 'Paid',
        'unpaid': 'Unpaid'
    }

    switcher_invoice_status = {
        'pending': "Pending",
        'attach': "Attached"
    }

    switcher_tax_invoice_status = {
        'pending': "Pending",
        'receive': "Received",
        'waiting_to_verify': "Waiting to verify",
        'resending': "Resending",
        'memo': "Memo"
    }

    switcher_purpose = {
        'work': "Work",
        'travel': "Travel",
        'leisure': "Leisure"
    }

    switcher_pay_out_status = {
        'unpaid': "Unpaid",
        'paid_by_non_credit_petty_cash': "Paid by Non Credit - Petty Cash",
        'paid_by_non_credit_credit_card': "Paid by Non Credit - Credit Card",
        'paid_by_non_credit_corporate_link': "Paid by Non Credit - Corporate Link",
        'paid_by_credit_terms_credit_card': "Paid by Credit Terms - Credit Card",
        'paid_by_credit_terms_corporate_link': "Paid by Credit Terms - Corporate Link",
        'paid_by_credit_terms_cheque': "Paid by Credit Terms - Cheque",
        'paid_by_credit_terms_account_transfer': "Paid by Credit Terms - Account Transfer"
    }

    switcher_supplier_paid_by = {
        'non_credit_petty_cash': "Non Credit - Petty Cash",
        'non_credit_credit_card': "Non Credit - Credit Card",
        'non_credit_corporate_link': "Non Credit - Corporate Link",
        'credit_terms_credit_card': "Credit Terms - Credit Card",
        'credit_terms_corporate_link': "Credit Terms - Corporate Link",
        'credit_terms_cheque': "Credit Terms - Cheque",
        'credit_terms_account_transfer': "Credit Terms - Account Transfer"
    }

    checkout_field_column_mapper = {
        'ch1CostCenter': 'CostCenterCode',
        'ch1CostCenterName': 'CostCenterName',
        'ch2CompanyRefCode': 'CompanyRefCode',
        'ch2CompanyRefName': 'CompanyRefName',
        'ch3AdditionalInfo': 'Ch3AdditionalInfo',
        'ch4AdditionalInfo': 'Ch4AdditionalInfo',
        'ch5AdditionalInfo': 'Ch5AdditionalInfo',
        'ch6AdditionalInfo': 'Ch6AdditionalInfo',
        'ch7AdditionalInfo': 'Ch7AdditionalInfo',
        'ch8AdditionalInfo': 'Ch8AdditionalInfo',
        'ch9AdditionalInfo': 'Ch9AdditionalInfo',
        'ch10AdditionalInfo': 'Ch10AdditionalInfo',
    }

    big_query_table_type = {'BookingID': 'int64'
        , 'CompanyID': 'int64'
        , 'ProductID': 'int64'
        , 'SupplierID': 'int64'
        , 'BookingDateTime': 'datetime64'
        , 'CheckInDate': 'datetime64'
        , 'CheckOutDate': 'datetime64'
        , 'UpdatedDate': 'datetime64'
        , 'Quantity': 'int64'
        , 'SellingPriceCreditTerms': 'float64'
        , 'TotalSellingPrice': 'float64'
        , 'SellingVat': 'float64'
        , 'SellingExVAT': 'float64'
        , 'TotalCost': 'float64'
        , 'CancelDate': 'datetime64'
        , 'CancellationCost': 'float64'
        , 'CancellationCostExVat': 'float64'
        , 'CancellationCostVat': 'float64'
        , 'CancellationFee': 'float64'
        , 'CancellationFeeExVat': 'float64'
        , 'CancellationFeeVat': 'float64'
        , 'ProfitAmount': 'float64'
        , 'ProfitPercent': 'float64'
        , 'CostExVAT': 'float64'
        , 'SupplierVAT': 'float64'
        , 'ConvertedCostExVAT': 'float64'
        , 'ConvertedVAT': 'float64'
        , 'ConvertedTotalCost': 'float64'
        , 'Commission': 'float64'
        , 'AverageSellingPricePerNightTHB': 'float64'
        , 'BookInAdvance': 'int64'
        , 'RoomNight': 'int64'}

    booking_booker_schema = [
        {'name': 'BookingID', 'type': 'INTEGER'},
        {'name': 'BookingDateTime', 'type': 'TIMESTAMP'},
        {'name': 'TravelingObjective', 'type': 'STRING'},
        {'name': 'CompanyName', 'type': 'STRING'},
        {'name': 'BookerEmail', 'type': 'STRING'},
        {'name': 'BookingStatus', 'type': 'STRING'},
        {'name': 'BookerName', 'type': 'STRING'},
        {'name': 'TravellerName', 'type': 'STRING'},
        {'name': 'TravellerEmail', 'type': 'STRING'},
        {'name': 'CostCenterCode', 'type': 'STRING'},

        {'name': 'CostCenterName', 'type': 'STRING'},
        {'name': 'BusinessArea', 'type': 'STRING'},
        {'name': 'ApprovalStatus', 'type': 'STRING'},
        {'name': 'SupervisorEmail', 'type': 'STRING'},
        {'name': 'Quantity', 'type': 'INTEGER'},
        {'name': 'CompanyRefCode', 'type': 'STRING'},
        {'name': 'CompanyRefName', 'type': 'STRING'},
        {'name': 'BookerDepartment', 'type': 'STRING'},
        {'name': 'SellingPriceCreditTerms', 'type': 'FLOAT'},
        {'name': 'DestinationID', 'type': 'STRING'},

        {'name': 'Destination', 'type': 'STRING'},
        {'name': 'ProductName', 'type': 'STRING'},
        {'name': 'CheckInDay', 'type': 'STRING'},
        {'name': 'CheckInDate', 'type': 'TIMESTAMP'},
        {'name': 'CheckOutDate', 'type': 'TIMESTAMP'},
        {'name': 'UpdatedDate', 'type': 'TIMESTAMP'},
        {'name': 'TotalSellingPrice', 'type': 'FLOAT'},
        {'name': 'SellingVat', 'type': 'FLOAT'},
        {'name': 'SellingExVAT', 'type': 'FLOAT'},
        {'name': 'ProfitAmount', 'type': 'FLOAT'},

        {'name': 'ProfitPercent', 'type': 'FLOAT'},
        {'name': 'CostExVAT', 'type': 'FLOAT'},
        {'name': 'SupplierVAT', 'type': 'FLOAT'},
        {'name': 'ConvertedCostExVAT', 'type': 'FLOAT'},
        {'name': 'ConvertedVAT', 'type': 'FLOAT'},
        {'name': 'ConvertedTotalCost', 'type': 'FLOAT'},
        {'name': 'Commission', 'type': 'FLOAT'},
        {'name': 'Contribution', 'type': 'STRING'},
        {'name': 'CompanySize', 'type': 'STRING'},
        {'name': 'IndustryTypeName', 'type': 'STRING'},

        {'name': 'ParentCompanyName', 'type': 'STRING'},
        {'name': 'AccountManagerOwner', 'type': 'STRING'},
        {'name': 'CompanyID', 'type': 'INTEGER'},
        {'name': 'ContractingManagerOwner', 'type': 'STRING'},
        {'name': 'ProductID', 'type': 'INTEGER'},
        {'name': 'SupplierID', 'type': 'INTEGER'},
        {'name': 'BookInAdvance', 'type': 'INTEGER'},
        {'name': 'RoomNight', 'type': 'INTEGER'},
        {'name': 'AverageSellingPricePerNightTHB', 'type': 'FLOAT'},
        {'name': 'TotalCost', 'type': 'FLOAT'},

        {'name': 'CancelDate', 'type': 'TIMESTAMP'},
        {'name': 'CancellationCost', 'type': 'FLOAT'},
        {'name': 'CancellationCostExVat', 'type': 'FLOAT'},
        {'name': 'CancellationCostVat', 'type': 'FLOAT'},
        {'name': 'CancellationFee', 'type': 'FLOAT'},
        {'name': 'CancellationFeeExVat', 'type': 'FLOAT'},
        {'name': 'CancellationFeeVat', 'type': 'FLOAT'},
        {'name': 'CancelReason', 'type': 'STRING'},
        {'name': 'ISO2', 'type': 'STRING'},
        {'name': 'SupplierName', 'type': 'STRING'},
        {'name': 'PaymentMethod', 'type': 'STRING'},
        {'name': 'OtherTravellerEmail', 'type': 'STRING'},

        {'name': 'OtherTravellerName', 'type': 'STRING'},
        {'name': 'UserBookerInternalOrder', 'type': 'STRING'},
        {'name': 'UserBookerCostCenterName', 'type': 'STRING'},
        {'name': 'UserBookerCostCenterCode', 'type': 'STRING'},
        {'name': 'UserBookerBusinessUnit', 'type': 'STRING'},
        {'name': 'UserBookerBusinessArea', 'type': 'STRING'},
        {'name': 'UserBookerBusinessPlace', 'type': 'STRING'},
        {'name': 'UserBookerProfitCenter', 'type': 'STRING'},
        {'name': 'UserBookerLevel', 'type': 'STRING'},
        {'name': 'UserBookerDepartmentID', 'type': 'STRING'},

        {'name': 'UserBookerDepartment', 'type': 'STRING'},
        {'name': 'UserBookerPosition', 'type': 'STRING'},
        {'name': 'UserBookerEmployeeID', 'type': 'STRING'},
        {'name': 'UserBookerTaxCode', 'type': 'STRING'},
        {'name': 'IsMainBooker', 'type': 'BOOLEAN'},
        {'name': 'ApproverName', 'type': 'STRING'},
        {'name': 'ApproverEmail', 'type': 'STRING'},
        {'name': 'ReporterEmail', 'type': 'STRING'},
        {'name': 'BookerBudget', 'type': 'INTEGER'},
    ]

    booking_summary_schema = [
        {'name': 'BookingID', 'type': 'INTEGER'},
        {'name': 'BookingDateTime', 'type': 'TIMESTAMP'},
        {'name': 'TravelingObjective', 'type': 'STRING'},
        {'name': 'CompanyName', 'type': 'STRING'},
        {'name': 'BookerEmail', 'type': 'STRING'},
        {'name': 'BookingStatus', 'type': 'STRING'},
        {'name': 'BookerName', 'type': 'STRING'},
        {'name': 'TravellerName', 'type': 'STRING'},
        {'name': 'TravellerEmail', 'type': 'STRING'},
        {'name': 'Quantity', 'type': 'INTEGER'},

        {'name': 'CompanyRefCode', 'type': 'STRING'},
        {'name': 'CompanyRefName', 'type': 'STRING'},
        {'name': 'SellingPriceCreditTerms', 'type': 'FLOAT'},
        {'name': 'ISO2', 'type': 'STRING'},
        {'name': 'SupplierName', 'type': 'STRING'},
        {'name': 'DestinationID', 'type': 'STRING'},
        {'name': 'Destination', 'type': 'STRING'},
        {'name': 'ProductName', 'type': 'STRING'},
        {'name': 'CheckInDay', 'type': 'STRING'},
        {'name': 'CheckInDate', 'type': 'TIMESTAMP'},

        {'name': 'CheckOutDate', 'type': 'TIMESTAMP'},
        {'name': 'UpdatedDate', 'type': 'TIMESTAMP'},
        {'name': 'TotalSellingPrice', 'type': 'FLOAT'},
        {'name': 'SellingVat', 'type': 'FLOAT'},
        {'name': 'SellingExVAT', 'type': 'FLOAT'},
        {'name': 'ProfitAmount', 'type': 'FLOAT'},
        {'name': 'ProfitPercent', 'type': 'FLOAT'},
        {'name': 'CostExVAT', 'type': 'FLOAT'},
        {'name': 'SupplierVAT', 'type': 'FLOAT'},
        {'name': 'TotalCost', 'type': 'FLOAT'},

        {'name': 'CancelDate', 'type': 'TIMESTAMP'},
        {'name': 'CancellationCost', 'type': 'FLOAT'},
        {'name': 'CancellationCostExVat', 'type': 'FLOAT'},
        {'name': 'CancellationCostVat', 'type': 'FLOAT'},
        {'name': 'CancellationFee', 'type': 'FLOAT'},
        {'name': 'CancellationFeeExVat', 'type': 'FLOAT'},
        {'name': 'CancellationFeeVat', 'type': 'FLOAT'},
        {'name': 'CancelReason', 'type': 'STRING'},
        {'name': 'ConvertedCostExVAT', 'type': 'FLOAT'},
        {'name': 'ConvertedVAT', 'type': 'FLOAT'},
        {'name': 'ConvertedTotalCost', 'type': 'FLOAT'},
        {'name': 'Commission', 'type': 'FLOAT'},

        {'name': 'Contribution', 'type': 'STRING'},
        {'name': 'CompanySize', 'type': 'STRING'},
        {'name': 'IndustryTypeName', 'type': 'STRING'},
        {'name': 'ParentCompanyName', 'type': 'STRING'},
        {'name': 'AccountManagerOwner', 'type': 'STRING'},
        {'name': 'CompanyID', 'type': 'INTEGER'},
        {'name': 'ContractingManagerOwner', 'type': 'STRING'},
        {'name': 'ProductID', 'type': 'INTEGER'},
        {'name': 'SupplierID', 'type': 'INTEGER'},
        {'name': 'BookInAdvance', 'type': 'INTEGER'},

        {'name': 'RoomNight', 'type': 'INTEGER'},
        {'name': 'AverageSellingPricePerNightTHB', 'type': 'FLOAT'},
        {'name': 'PaymentMethod', 'type': 'STRING'},
        {'name': 'OtherTravellerEmail', 'type': 'STRING'},
        {'name': 'OtherTravellerName', 'type': 'STRING'},
        {'name': 'PaymentMethodDetail', 'type': 'STRING'},
    ]

    booking_operation_schema = [
        {'name': 'BookingID', 'type': 'INTEGER'},
        {'name': 'SupplierPaidBy', 'type': 'STRING'},
        {'name': 'InvoiceStatus', 'type': 'STRING'},
        {'name': 'PayoutStatus', 'type': 'STRING'},
        {'name': 'PaySlipStatus', 'type': 'STRING'},
        {'name': 'TaxInvoiceStatus', 'type': 'STRING'},
    ]

    booking_schema = [
        {'name':'BookingDate','type': 'DATETIME'},
        {'name':'CancelDate','type': 'DATETIME'},
        {'name':'UpdateDate','type': 'DATETIME'},
        {'name':'BookingCheckInDate','type': 'DATE'},
        {'name':'BookingCheckOutDate','type': 'DATE'}
    ]

    booking_condition_schema = [
        {'name': 'BookingConditionID', 'type': 'INTEGER'},
        {'name': 'BookingRoomtypeID', 'type': 'INTEGER'},
        {'name': 'ConditionID', 'type': 'INTEGER'},
        {'name': 'ConditionOfferTypeName', 'type': 'STRING'},
        {'name': 'Quantity', 'type': 'INTEGER'},
        {'name': 'SellingPrice', 'type': 'FLOAT'},
        {'name': 'Cost', 'type': 'FLOAT'}
    ]

    booking_supplier_schema = [
        {'name': 'BookingSupplierID', 'type': 'INTEGER'},
        {'name': 'BookingID', 'type': 'INTEGER'},
        {'name': 'SupplierID', 'type': 'INTEGER'},
        {'name': 'SupplierTypeID', 'type': 'INTEGER'},
        {'name': 'SupplierName', 'type': 'STRING'},
        {'name': 'SupplierConfirmStatus', 'type': 'STRING'},
        {'name': 'SupplierConfirmDate', 'type': 'DATETIME'}
    ]

    paymentMethodOrder = {
        "credit_terms": 0,
        "corporate_fleet": 1,
        "personal_fleet": 2,
        "credit_card": 3,
        "true_money": 4
    }

class GbqBookingBooker:
    _defaults = ['BookingID', 'BookingDateTime', 'TravelingObjective', 'CompanyName', 'BookerEmail', 'BookingStatus',
                 'BookerName', 'TravellerName', 'TravellerEmail', 'CostCenterCode', 'CostCenterName', 'BusinessArea',
                 'ApprovalStatus', 'SupervisorEmail', 'Quantity', 'CompanyRefCode', 'CompanyRefName',
                 'BookerDepartment', 'SellingPriceCreditTerms', 'ISO2', 'SupplierName', 'DestinationID', 'Destination',
                 'ProductName', 'CheckInDay', 'CheckInDate', 'CheckOutDate', 'UpdatedDate', 'TotalSellingPrice',
                 'SellingVat', 'SellingExVAT', 'ProfitAmount', 'ProfitPercent', 'CostExVAT', 'SupplierVAT', 'TotalCost',
                 'CancelDate', 'CancellationCost', 'CancellationCostExVat', 'CancellationCostVat', 'CancellationFee',
                 'CancellationFeeExVat', 'CancellationFeeVat', 'ConvertedCostExVAT', 'ConvertedVAT',
                 'ConvertedTotalCost', 'Commission', 'Contribution', 'CompanySize', 'IndustryTypeName',
                 'ParentCompanyName', 'AccountManagerOwner', 'CompanyID', 'ContractingManagerOwner', 'ProductID',
                 'SupplierID', 'BookInAdvance', 'RoomNight', 'AverageSellingPricePerNightTHB', 'PaymentMethod',
                 'OtherTravellerEmail', 'OtherTravellerName', 'UserBookerInternalOrder', 'UserBookerCostCenterName',
                 'UserBookerCostCenterCode', 'UserBookerBusinessUnit', 'UserBookerBusinessArea',
                 'UserBookerBusinessPlace', 'UserBookerProfitCenter', 'UserBookerLevel', 'UserBookerDepartmentID',
                 'UserBookerDepartment', 'UserBookerPosition', 'UserBookerEmployeeID', 'UserBookerTaxCode',
                 'CancelReason', 'IsMainBooker', 'ApproverName', 'ApproverEmail', 'ReporterEmail', 'BookerBudget', 
                 'Ch3AdditionalInfo', 'Ch4AdditionalInfo', 'Ch5AdditionalInfo', 'Ch6AdditionalInfo', 
                 'Ch7AdditionalInfo', 'Ch8AdditionalInfo', 'Ch9AdditionalInfo', 'Ch10AdditionalInfo']
    _default_value = None

    def __init__(self, **kwargs):
        self.__dict__.update(dict.fromkeys(self._defaults, self._default_value))
        self.__dict__.update(kwargs)


class GbqBookingSummary:
    _defaults = ['BookingID', 'BookingDateTime', 'TravelingObjective', 'CompanyName', 'BookerEmail', 'BookingStatus',
                 'BookerName', 'TravellerName', 'TravellerEmail', 'Quantity', 'CompanyRefCode', 'CompanyRefName',
                 'SellingPriceCreditTerms', 'ISO2', 'SupplierName', 'DestinationID', 'Destination', 'ProductName',
                 'CheckInDay', 'CheckInDate', 'CheckOutDate', 'UpdatedDate', 'TotalSellingPrice', 'SellingVat',
                 'SellingExVAT', 'ProfitAmount', 'ProfitPercent', 'CostExVAT', 'SupplierVAT', 'TotalCost', 'CancelDate',
                 'CancellationCost', 'CancellationCostExVat', 'CancellationCostVat', 'CancellationFee',
                 'CancellationFeeExVat', 'CancellationFeeVat', 'ConvertedCostExVAT', 'ConvertedVAT',
                 'ConvertedTotalCost', 'Commission', 'Contribution', 'CompanySize', 'IndustryTypeName',
                 'ParentCompanyName', 'AccountManagerOwner', 'CompanyID', 'ContractingManagerOwner', 'ProductID',
                 'SupplierID', 'BookInAdvance', 'RoomNight', 'AverageSellingPricePerNightTHB', 'PaymentMethod',
                 'OtherTravellerEmail', 'OtherTravellerName', 'CancelReason', 'PaymentMethodDetail']
    _default_value = None

    def __init__(self, **kwargs):
        self.__dict__.update(dict.fromkeys(self._defaults, self._default_value))
        self.__dict__.update(kwargs)

class GbqBooking: pass


class GbqBookingBooker: pass


class GbqBookingGuest: pass


class GbqBookingHotel: pass


class GbqBookingSupplier: pass


class GbqHotelSupplier: pass


class GbqBookingRoomtype: pass


class GbqBookingCondition: pass


class GbqBookingRating: pass


class GbqProduct: pass


class GbqDestination: pass


class GbqSupplier: pass


class GbqCompany: pass


class GbqBookingDashboardModel:
    def __init__(self, gbq_booking_summary_list, gbq_booking_booker_list, error_booking_id):
        self.gbq_booking_summary_list = gbq_booking_summary_list
        self.gbq_booking_booker_list = gbq_booking_booker_list
        self.error_booking_id = error_booking_id


def days_between(d1, d2):
    d1 = datetime.strptime(d1.strftime('%Y-%m-%d'), '%Y-%m-%d')
    d2 = datetime.strptime(d2.strftime('%Y-%m-%d'), '%Y-%m-%d')
    return abs((d2 - d1).days)


def round_half_up(n, decimals=2):
    if pd.isnull(n):
        return None
    multiplier = 10 ** decimals
    return math.floor(n * multiplier + 0.5) / multiplier


def compare(item1, item2):
    if Models.paymentMethodOrder[item1] < Models.paymentMethodOrder[item2]:
        return -1
    else:
        return 1


def chunks(list, chunk_size):
    for i in range(0, len(list), chunk_size):
        yield list[i:i + chunk_size]

def get_df_from_query(stmt, connection, list, chunk_size=1000):
    df_list = []

    for chunk in chunks(list, chunk_size):
        df = pd.read_sql(stmt % ','.join(map(str, chunk)), connection)
        df_list.append(df)
    if len(df_list) == 0:
        return pd.DataFrame()
    return pd.concat(df_list)
