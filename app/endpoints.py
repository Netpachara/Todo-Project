from dependency_injector.wiring import inject, Provide
from fastapi import APIRouter, Depends, Request
from datetime import datetime, time, timedelta
from .containers import Container
from .services import ETLBookingService, ETLBookingValidationService
from .models.etl_date_type_request import ETLDateTypeRequest, ETLDateType
from .supplier_dashboard_services import SupplierDashboardService


router = APIRouter()


@router.post("/etl-booking-dashboard")
@inject
async def etl_booking_dashboard(request: Request,
                                etl_booking_service: ETLBookingService = Depends(Provide[Container.etl_booking_service]), ):
    body = await request.json()
    return etl_booking_service.handler(body)


@router.post("/etl-booking-dashboard/validation")
@inject
async def etl_booking_dashboard_validation(etl_booking_validation_service: ETLBookingValidationService = Depends(Provide[Container.etl_booking_validation_service]), ):
    return etl_booking_validation_service.handler()


@router.post("/etl-booking-dashboard/{date_type}")
@inject
async def etl_booking_dashboard_by_date_type(date_type: ETLDateType, request: ETLDateTypeRequest, 
                                            etl_booking_service: ETLBookingService = Depends(Provide[Container.etl_booking_service]), ):
    request.dateType = date_type
    if date_type == ETLDateType.booking_date:
        dateFrom = datetime.combine(datetime.now(), time.min) - timedelta(hours=7)
        dateTo = datetime.combine(datetime.now(), time.max) - timedelta(hours=7)
        request.dateFrom = dateFrom.strftime('%Y-%m-%d %H:%M:%S') if request.dateFrom is None else request.dateFrom
        request.dateTo = dateTo.strftime('%Y-%m-%d %H:%M:%S') if request.dateTo is None else request.dateTo
    elif date_type == ETLDateType.checkin_date:
        request.dateFrom = datetime.combine(datetime.now(), time.min).strftime('%Y-%m-%d %H:%M:%S') if request.dateFrom is None else request.dateFrom
        request.dateTo = datetime.combine(datetime.now(), time.max).strftime('%Y-%m-%d %H:%M:%S') if request.dateTo is None else request.dateTo
    return etl_booking_service.handler(request.dict())

@router.post("/etl-booking-operation")
@inject
async def etl_booking_operation(request: Request,
                                etl_booking_service: ETLBookingService = Depends(Provide[Container.etl_booking_service]), ):
    body = await request.json()
    return etl_booking_service.handler_booking_operation(body)




@router.post("/test_sync_product")
@inject
async def test_sync_product(request: Request,
                                etl_booking_service: ETLBookingService = Depends(Provide[Container.etl_booking_service]), ):
    body = await request.json()
    print(body)
    etl_booking_service.handler_product(body,{})
    
    # return etl_booking_sservice.handler(body)


@router.post("/test_sync_to_dashboard")
@inject
async def test_sync_to_dashboard(request: Request,
                                supplier_dashboard_service: SupplierDashboardService = Depends(Provide[Container.supplier_dashboard_service]), ):
    body = await request.json()
    # print(body)
    # etl_booking_service.handler_product(body,{})
    
            
    return supplier_dashboard_service.handler_Test(body)
