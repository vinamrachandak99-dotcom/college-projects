@echo off
echo ===================================================
echo Starting XFL-IoMT Clinical Decision Gateway
echo ===================================================

echo [1/4] Starting Flower Aggregator (Server) on Port 8080...
start "Flower Aggregator - Port 8080" cmd /k "python aggregator/server.py"

echo Waiting 5 seconds for the aggregator to initialize...
timeout /t 5 /nobreak >nul

echo [2/4] Starting Hospital A (Cardiology) on Port 8001...
start "Hospital A - Port 8001" cmd /k "set NODE_NAME=Hospital A (Cardiology)&& set DB_NAME=hospital_a.db&& set MODEL_PATH=model_a.pkl&& set MLP_MODEL_PATH=mlp_a.pt&& set SYNC_STATE_PATH=sync_a.json&& set RANDOM_SEED=42&& python -m uvicorn edge_node.client_api:app --host 0.0.0.0 --port 8001"

echo [3/4] Starting Hospital B (General Medicine) on Port 8002...
start "Hospital B - Port 8002" cmd /k "set NODE_NAME=Hospital B (General Medicine)&& set DB_NAME=hospital_b.db&& set MODEL_PATH=model_b.pkl&& set MLP_MODEL_PATH=mlp_b.pt&& set SYNC_STATE_PATH=sync_b.json&& set RANDOM_SEED=99&& python -m uvicorn edge_node.client_api:app --host 0.0.0.0 --port 8002"

echo [4/4] Starting Next.js Dashboard on Port 3000...
start "Clinical Dashboard - Port 3000" cmd /k "cd frontend && npm run dev"

echo Waiting 5 seconds for frontend to initialize...
timeout /t 5 /nobreak >nul

echo Launching browser at http://localhost:3000...
start http://localhost:3000

echo ===================================================
echo All services are running!
echo To stop everything, simply run stop_all.bat
echo ===================================================