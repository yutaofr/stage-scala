#!/usr/bin/env python3
import json
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

EVIDENCE = Path("/data/notifications.jsonl")


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/health":
            self._reply(200, b"UP\n")
        else:
            self._reply(404, b"not found\n")

    def do_POST(self):
        if self.path != "/alerts":
            self._reply(404, b"not found\n")
            return
        length = int(self.headers.get("Content-Length", "0"))
        try:
            payload = json.loads(self.rfile.read(length))
        except (json.JSONDecodeError, UnicodeDecodeError):
            self._reply(400, b"invalid json\n")
            return

        EVIDENCE.parent.mkdir(parents=True, exist_ok=True)
        evidence = {
            "receivedAt": datetime.now(timezone.utc).isoformat(),
            "payload": payload,
        }
        with EVIDENCE.open("a", encoding="utf-8") as output:
            output.write(json.dumps(evidence, separators=(",", ":")) + "\n")
        self._reply(202, b"accepted\n")

    def _reply(self, status, body):
        self.send_response(status)
        self.send_header("Content-Type", "text/plain; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, pattern, *args):
        print(pattern % args, flush=True)


if __name__ == "__main__":
    ThreadingHTTPServer(("0.0.0.0", 8081), Handler).serve_forever()
