#!/usr/bin/env python3
"""
demo_preview.py — Preview the Demo App UI without Java/Maven.
Run: python3 demo_preview.py
Then open: http://localhost:8080
"""

import json
import time
import platform
import sys
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse

tasks = []
next_id = 1

def add_task(title, status="todo"):
    global next_id
    tasks.append({"id": next_id, "title": title, "status": status,
                   "created": time.strftime("%Y-%m-%dT%H:%M:%S")})
    next_id += 1

# Seed data
add_task("Set up the server",  "done")
add_task("Build the REST API", "done")
add_task("Design the UI",      "in-progress")
add_task("Write unit tests",   "todo")

class Handler(BaseHTTPRequestHandler):
    def log_message(self, fmt, *args):
        print(f"  {self.command} {self.path} → {args[1]}")

    def send_json(self, code, data):
        body = json.dumps(data).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Content-Length", len(body))
        self.end_headers()
        self.wfile.write(body)

    def send_html(self, html: str):
        body = html.encode()
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", len(body))
        self.end_headers()
        self.wfile.write(body)

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET,POST,DELETE,PATCH,OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.end_headers()

    def do_GET(self):
        path = urlparse(self.path).path

        if path in ("/", "/index.html"):
            try:
                with open("src/main/resources/static/index.html", "rb") as f:
                    html = f.read()
                self.send_response(200)
                self.send_header("Content-Type", "text/html; charset=utf-8")
                self.end_headers()
                self.wfile.write(html)
            except FileNotFoundError:
                self.send_error(404, "index.html not found")
            return

        if path == "/api/tasks":
            return self.send_json(200, tasks)

        if path == "/api/info":
            return self.send_json(200, {
                "app": "Demo App (Python Preview)",
                "version": "1.0.0",
                "java": "N/A — Python " + sys.version.split()[0],
                "os": platform.system() + " " + platform.release()
            })

        if path == "/actuator/health":
            return self.send_json(200, {"status": "UP"})

        self.send_error(404)

    def do_POST(self):
        if self.path == "/api/tasks":
            length = int(self.headers.get("Content-Length", 0))
            body = json.loads(self.rfile.read(length) or b"{}")
            title = body.get("title", "").strip()
            if not title:
                return self.send_json(400, {"error": "title required"})
            add_task(title)
            return self.send_json(201, tasks[-1])
        self.send_error(404)

    def do_DELETE(self):
        parts = urlparse(self.path).path.split("/")
        if len(parts) >= 4 and parts[2] == "tasks":
            task_id = int(parts[3])
            before = len(tasks)
            tasks[:] = [t for t in tasks if t["id"] != task_id]
            if len(tasks) < before:
                return self.send_json(200, {"deleted": task_id})
            return self.send_json(404, {"error": "not found"})
        self.send_error(404)

    def do_PATCH(self):
        parts = urlparse(self.path).path.split("/")
        if len(parts) >= 4 and parts[2] == "tasks":
            task_id = int(parts[3])
            length = int(self.headers.get("Content-Length", 0))
            body = json.loads(self.rfile.read(length) or b"{}")
            for t in tasks:
                if t["id"] == task_id:
                    t["status"] = body.get("status", t["status"])
                    return self.send_json(200, t)
            return self.send_json(404, {"error": "not found"})
        self.send_error(404)

if __name__ == "__main__":
    port = 8080
    server = HTTPServer(("", port), Handler)
    print(f"\n╔══════════════════════════════════════╗")
    print(f"║   Demo App Preview — Python Server   ║")
    print(f"║   Open: http://localhost:{port}         ║")
    print(f"║   Ctrl+C to stop                     ║")
    print(f"╚══════════════════════════════════════╝\n")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n👋 Server stopped.")
