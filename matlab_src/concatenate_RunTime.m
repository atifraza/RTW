clear all; close all; clc
fileNames = {'NonInvasiveFatalECG_Thorax1', ...
             'NonInvasiveFatalECG_Thorax2', ...
             'StarLightCurves'};

dtwTypes = {'Uniform', 'Gaussian'}; %, 'SkewedNormal'
windowSize = [100, 20, 15, 10, 5, 0];
dirPath = 'exp/10Runs_0Restarts/';
for fileName = fileNames
    for win = windowSize
        for type = dtwTypes
            fileNameString = strcat(dirPath, char(fileName), '_', num2str(win), '_', char(type), '_RunTime.csv');
            runTimes = csvread( char(fileNameString), 0, 1);
            runTimes = reshape(runTimes, 10, []);
            summedTimes = [(1:10)', sum(runTimes,2)];
            csvwrite(fileNameString, summedTimes);
        end
    end
end

