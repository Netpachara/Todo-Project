from fastapi import FastAPI

from app import endpoints
from app.containers import Container


def create_app() -> FastAPI:
    container = Container()

    app = FastAPI()
    app.container = container
    app.include_router(endpoints.router, prefix="/api/v1")
    return app


app = create_app()

@app.get("/healthcheck")
async def healthcheck():
    return {"status": "alive"}
