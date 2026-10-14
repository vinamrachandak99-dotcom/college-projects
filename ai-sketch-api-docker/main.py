import cv2
import numpy as np
from fastapi import FastAPI, File, UploadFile, Form
from fastapi.responses import Response, HTMLResponse

app = FastAPI(title="AI Image Processor API")

@app.get("/", response_class=HTMLResponse)
async def serve_ui():
    with open("index.html", "r") as file:
        html_content = file.read()
    return HTMLResponse(content=html_content, status_code=200)

@app.post("/process")
async def process_image(file: UploadFile = File(...), effect: str = Form("sketch")):
    contents = await file.read()
    nparr = np.frombuffer(contents, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    
    if effect == "sketch":
        gray_img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        inverted_img = 255 - gray_img
        blurred_img = cv2.GaussianBlur(inverted_img, (21, 21), 0)
        inverted_blurred = 255 - blurred_img
        result = cv2.divide(gray_img, inverted_blurred, scale=256.0)
        
        # NEW: Darken the sketch to make the pencil lines bolder
        # alpha=0.75 reduces contrast (darkens whites), beta=-10 reduces overall brightness
        result = cv2.convertScaleAbs(result, alpha=0.75, beta=-10)
        
    elif effect == "sepia":
        sepia_kernel = np.array([[0.272, 0.534, 0.131],
                                 [0.349, 0.686, 0.168],
                                 [0.393, 0.769, 0.189]])
        result = cv2.transform(img, sepia_kernel)
        result = np.clip(result, 0, 255).astype(np.uint8)
        
        # NEW: Darken the sepia for a richer, deeper vintage look
        result = cv2.convertScaleAbs(result, alpha=0.85, beta=-15)
        
    elif effect == "thermal":
        gray_img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        result = cv2.applyColorMap(gray_img, cv2.COLORMAP_JET)
        
    else:
        result = img 
        
    _, encoded_img = cv2.imencode('.jpg', result)
    return Response(content=encoded_img.tobytes(), media_type="image/jpeg")