@echo off
setlocal enabledelayedexpansion

echo =====================================================================
echo  BORI-DEX VISION SCANNER: STAGE 4 FINE-TUNING & DEPLOYMENT PIPELINE
echo =====================================================================
echo.

cd /d C:\Users\pagan\OneDrive\Documents\Code\Pokedex\Pokemon_ref_imgs

echo [Step 1/3] Merging all scraped tattoo images into master varaiants dataset...
.venv_scraper\Scripts\python.exe merge_all_datasets.py --sources varaiants_tattoos
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Dataset merge failed with exit code %ERRORLEVEL%.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [Step 2/3] Launching Stage 4 Fine-Tuning with Glare and Perspective Keystoning...
.venv_directml_39\Scripts\python.exe train_stage4_tattoos_finetuning.py
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Fine-tuning failed with exit code %ERRORLEVEL%.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [Step 3/3] Exporting Float32 TFLite and deploying to Android app assets...
.venv_directml_39\Scripts\python.exe export_stage4_float32.py
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] TFLite export failed with exit code %ERRORLEVEL%.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo =====================================================================
echo  STAGE 4 PIPELINE COMPLETED SUCCESSFULLY!
echo  model.tflite has been updated in PokedexApp\app\src\main\assets\
echo =====================================================================
echo.
pause
