from enum import Enum
from pydantic import BaseModel

class ETLDateType(str, Enum):
    checkin_date = "checkin_date"
    booking_date = "booking_date"

class ETLDateTypeRequest(BaseModel):
    dateFrom: str = None
    dateTo: str = None
    dateType: str = None