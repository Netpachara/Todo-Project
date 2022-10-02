import os

from config.spring import ConfigClient
from dependency_injector import containers, providers

from app.services import ETLBookingValidationService, ETLBookingService
from app.supplier_dashboard_services import SupplierDashboardService


class Container(containers.DeclarativeContainer):
    wiring_config = containers.WiringConfiguration(modules=[".endpoints"])
    app_name = 'etl-booking-dashboard-service'
    server_address = 'http://alpha-service.ascendtravel-dev.com:7999/spring-cloud-config-server'
    print(app_name, server_address)
    c = ConfigClient(app_name=app_name, address=server_address)
    c.get_config()

    etl_booking_service = providers.Factory(
        ETLBookingService,
        config_client=c
    )

    etl_booking_validation_service = providers.Factory(
        ETLBookingValidationService,
        config_client=c
    )

    supplier_dashboard_service = providers.Factory(
        SupplierDashboardService,
        config_client=c
    )
