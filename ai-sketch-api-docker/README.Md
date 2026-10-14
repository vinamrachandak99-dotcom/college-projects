# AI Image Processing Studio (Dockerized Microservice)

## Overview
This project is a containerized web application that applies advanced Computer Vision filters to user-uploaded images. It demonstrates modern cloud-native architecture, utilizing a FastAPI backend and an HTML/JS frontend, all packaged securely inside a Docker container.

## Features
* **Pencil Sketch Filter:** Utilizes grayscale conversion, Gaussian blur, and image division.
* **Vintage Sepia Filter:** Applies color matrix multiplication for a warm, vintage aesthetic.
* **Thermal Vision:** Implements color mapping based on light intensity.

## Technical Stack
* **Backend:** Python, FastAPI, Uvicorn
* **Computer Vision:** OpenCV (`opencv-python-headless`)
* **Containerization:** Docker

## Security & Architecture Highlights
1. **Lightweight Environment:** Uses `python:3.11-slim` to reduce container image size.
2. **Headless Libraries:** Utilizes `opencv-python-headless` to prevent missing system library crashes (like `libGL`) in server environments.
3. **Non-Root Execution:** Implements a dedicated non-root `appuser` within the Dockerfile to adhere to enterprise security standards.

## How to Run
1. Build the image: `docker build -t ai-sketch-api .`
2. Run the container: `docker run -d -p 8000:8000 --name sketch-app ai-sketch-api`
3. Access the UI: Open `http://localhost:8000` in a web browser.