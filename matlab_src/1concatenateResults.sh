currResultsDir='exp/10Runs_0Restarts/'
outResultDir='exp/10Runs_0Restarts/'
#for currStartPt in {0..1800..300}
for currStartPt in {0..8235..300}
do
#    for fName in NonInvasiveFatalECG_Thorax1 NonInvasiveFatalECG_Thorax2
    for fName in StarLightCurves
    do
        for winSz in 0 5 10 15 20 100
        do
#            for type in Lucky Normal
            for type in Gaussian Uniform
            do
#                for ending in Accuracy Time_Length TotalTime
                for ending in Accuracy Time_Length TotalTime RunTime
                do
#                    echo 'Split_'${currStartPt}'/'${fName}'_'${winSz}'_'${type}'_'${ending}'.csv to '${fName}'_'${winSz}'_'${type}'_'${ending}'.csv'
#                    echo ''
#                    cat ${currResultsDir}'Split_'${currStartPt}'/'${fName}'_'${winSz}'_'${type}'_'${ending}'.csv' | wc -l
#                    echo ''
#                    head -n 2 'Split_'${currStartPt}'/'${fName}'_'${winSz}'_'${type}'_'${ending}'.csv'
#                    echo '...'
#                    tail -n 2 'Split_'${currStartPt}'/'${fName}'_'${winSz}'_'${type}'_'${ending}'.csv'
                    cat ${currResultsDir}'Split_'${currStartPt}'/'${fName}'_'${winSz}'_'${type}'_'${ending}'.csv' >> ${outResultDir}${fName}'_'${winSz}'_'${type}'_'${ending}'.csv'
                done
            done
        done
    done
done
