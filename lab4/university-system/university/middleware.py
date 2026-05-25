"""Рівень 4: Middleware для логування запитів."""
import time
import logging

logger = logging.getLogger('university')
logging.basicConfig(level=logging.INFO, format='%(asctime)s [%(levelname)s] %(message)s')


class RequestLoggingMiddleware:
    """Логування всіх API-запитів з часом виконання."""

    def __init__(self, get_response):
        self.get_response = get_response

    def __call__(self, request):
        start_time = time.time()
        response = self.get_response(request)
        duration = time.time() - start_time

        if request.path.startswith('/api/'):
            logger.info(
                f"{request.method} {request.path} — "
                f"Status: {response.status_code} — "
                f"Time: {duration:.3f}s"
            )

        return response
