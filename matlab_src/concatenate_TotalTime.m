clear all; close all; clc; pwd
fileNames = {'NonInvasiveFatalECG_Thorax1', ...
             'NonInvasiveFatalECG_Thorax2', ...
             'StarLightCurves'};

% dtwTypes = {'Lucky', 'Normal'};
dtwTypes = {'Uniform', 'Gaussian'}; %, 'SkewedNormal'

windowSize = [100, 20, 15, 10, 5, 0];
dirPath = 'exp/10Runs_0Restarts/'; % 'deterministic/Test/'
for fileName = fileNames
    for win = windowSize
        for type = dtwTypes
            fileNameString = strcat(dirPath, char(fileName), '_', num2str(win), '_', char(type), '_TotalTime.csv');
            totalTime = csvread( char(fileNameString));
            totalTime = sum(totalTime);
            csvwrite(fileNameString, totalTime)
        end
    end
end
