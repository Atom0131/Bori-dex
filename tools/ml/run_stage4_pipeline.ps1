# Bori-dex Vision Scanner: Stage 4 Fine-Tuning & Deployment Pipeline
$ErrorActionPreference = "Stop"

Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host " BORI-DEX VISION SCANNER: STAGE 4 FINE-TUNING & DEPLOYMENT PIPELINE" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host ""

Set-Location "C:\Users\pagan\OneDrive\Documents\Code\Pokedex\Pokemon_ref_imgs"

Write-Host "[Step 1/3] Merging all scraped tattoo images into master varaiants dataset..." -ForegroundColor Yellow
& .venv_scraper\Scripts\python.exe merge_all_datasets.py --sources varaiants_tattoos
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Dataset merge failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "[Step 2/3] Launching Stage 4 Fine-Tuning with Glare and Perspective Keystoning..." -ForegroundColor Yellow
& .venv_directml_39\Scripts\python.exe train_stage4_tattoos_finetuning.py
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Fine-tuning failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "[Step 3/3] Exporting Float32 TFLite and deploying to Android app assets..." -ForegroundColor Yellow
& .venv_directml_39\Scripts\python.exe export_stage4_float32.py
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] TFLite export failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "=====================================================================" -ForegroundColor Green
Write-Host " STAGE 4 PIPELINE COMPLETED SUCCESSFULLY!" -ForegroundColor Green
Write-Host " model.tflite has been updated in PokedexApp\app\src\main\assets\" -ForegroundColor Green
Write-Host "=====================================================================" -ForegroundColor Green
