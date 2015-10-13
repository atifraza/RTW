#!/bin/bash
cpu=0
declare -a datasetNames=("ItalyPowerDemand") #"ItalyPowerDemand" "SonyAIBORobotSurface" "Coffee" "ECG200" "SonyAIBORobotSurfaceII" "Gun_Point" "TwoLeadECG" "MoteStrain" "Beef" "Plane" "FaceFour" "OliveOil" "synthetic_control" "ECGFiveDays" "CBF" "Lighting7" "DiatomSizeReduction" "Trace" "Car" "Lighting2" "MedicalImages" "Symbols" "Adiac" "SwedishLeaf" "FISH" "FacesUCR" "OSULeaf" "WordsSynonyms" "Cricket_X" "Cricket_Y" "Cricket_Z" "50words" "FaceAll" "ChlorineConcentration" "Haptics" "Two_Patterns" "MALLAT" "wafer" "CinC_ECG_torso" "yoga" "InlineSkate" "uWaveGestureLibrary_X" "uWaveGestureLibrary_Y" "uWaveGestureLibrary_Z"
declare -a windowSizes=($(seq 1 3)) #5 10 15 20 100 OR $(seq 1 100) OR $(seq 1 2 100)
declare -a warpingTypes=("L" "N" "R")
declare -a generators=("G") #"U" "S"
declare -a ranking=("E") # "L"
oDir="win-size_0-100"
jarName="warping.jar"
for dSet in "${datasetNames[@]}"; do
    # Run experiment for Euclidean distance
	taskset -c $cpu java -jar $jarName --file $dSet --type E --out-dir $oDir/$dSet
	wait $!
	for w in "${windowSizes[@]}"; do
		for wTyp in "${warpingTypes[@]}"; do
			if [ "$wTyp" == "R" ]; then # Run experiment for RTW passes 0, ranking E, rng G
				for rng in "${generators[@]}"; do
				    for rank in "${ranking[@]}"; do
					    taskset -c $cpu java -jar $jarName --file $dSet --out-dir $oDir/$dSet --window $w --type $wTyp --rng $rng --ranking $rank --distance 2
					    wait $!
					    taskset -c $cpu java -jar $jarName --file $dSet --out-dir $oDir/$dSet --window $w --type $wTyp --rng $rng --ranking $rank --distance 1
					    wait $!
				    done
				done
			else # Run experiment for LuckyTW and DTW
				taskset -c $cpu java -jar $jarName --file $dSet --out-dir $oDir/$dSet --window $w --type $wTyp #--distance 2
				wait $!
			fi
		done
	done
done

