#!/bin/bash
#declare -a datasetNames=("ItalyPowerDemand" "SonyAIBORobotSurface" "Coffee" "ECG200" "SonyAIBORobotSurfaceII" "Gun_Point" "TwoLeadECG" "MoteStrain" "Beef" "Plane" "FaceFour" "OliveOil" "synthetic_control" "ECGFiveDays" "CBF" "Lighting7" "DiatomSizeReduction" "Trace" "Car" "Lighting2" "MedicalImages" "Symbols" "Adiac" "SwedishLeaf" "FISH" "FacesUCR" "OSULeaf" "WordsSynonyms" "Cricket_X" "Cricket_Y" "Cricket_Z" "50words")
declare -a datasetNames=("FaceAll" "ChlorineConcentration" "Haptics" "Two_Patterns" "MALLAT" "wafer" "CinC_ECG_torso" "yoga" "InlineSkate" "uWaveGestureLibrary_X" "uWaveGestureLibrary_Y" "uWaveGestureLibrary_Z")
declare -a windowSizes=(1 2 3 4 5 6 7 8 9 10 15 20 100)
for dataset in "${datasetNames[@]}"
do
	# Run experiment for Euclidean distance
	java -jar randwarp.jar --file $dataset --type E
	wait $!
	for window in "${windowSizes[@]}"
	do
		# Run experiment for LuckyTW and DTW
		for detType in "L" "N"
		do
			java -jar randwarp.jar --file $dataset --window $window --type $detType
			wait $!
		done
		# Run experiment for RTW
#		for rng in "G"
#		do
#			java -jar randwarp.jar --file $dataset --window $window --type R -rng $rng --passes $restarts --ranking E
#			wait $!
#		done
	done
done
