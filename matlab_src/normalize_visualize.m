clear all; close all; clc;

fileNames = {'synthetic_control', 'Gun_Point', 'CBF', 'FaceAll', 'OSULeaf',...
    'SwedishLeaf', '50words', 'Trace', 'Two_Patterns', 'wafer', 'FaceFour', ...
    'Lighting2', 'Lighting7', 'ECG200', 'Adiac', 'yoga', 'FISH', 'Plane', ...
    'Car', 'Beef', 'Coffee', 'OliveOil', 'CinC_ECG_torso', ...
    'ChlorineConcentration', 'DiatomSizeReduction', 'ECGFiveDays', 'FacesUCR', ...
    'Haptics', 'InlineSkate', 'ItalyPowerDemand', 'MALLAT', 'MedicalImages', ...
    'MoteStrain', 'SonyAIBORobotSurfaceII', 'SonyAIBORobotSurface', ...
    'StarLightCurves', 'Symbols', 'TwoLeadECG', 'WordsSynonyms', 'Cricket_X', ...
    'Cricket_Y', 'Cricket_Z', 'uWaveGestureLibrary_X', 'uWaveGestureLibrary_Y',...
    'uWaveGestureLibrary_Z', 'NonInvasiveFatalECG_Thorax1', 'NonInvasiveFatalECG_Thorax2'};

data_Original = '../data/Original/';
plotsDir = '../data/Plots/';
data_Averaged = '../data/Averaged/';
data_Normed = '../data/Normalized/';

for fileName = fileNames
    trainSet_Original = sortrows(load(strcat(data_Original, char(fileName), '_TRAIN')));
    testSet_Original = sortrows(load(strcat(data_Original, char(fileName), '_TEST')));
    trainSet_Normed = zeros(size(trainSet_Original));
    trainSet_Normed(:,1) = trainSet_Original(:,1);
    testSet_Normed = zeros(size(testSet_Original));
    testSet_Normed(:,1) = testSet_Original(:,1);
    
    trainSet_Normed(:, 2:end) = ( trainSet_Original(:, 2:end)-repmat(mean(trainSet_Original(:, 2:end), 2), 1, size(trainSet_Original, 2)-1) )./repmat(std(trainSet_Original(:, 2:end), 0, 2), 1, size(trainSet_Original, 2)-1);
    testSet_Normed(:, 2:end) = ( testSet_Original(:, 2:end)-repmat(mean(testSet_Original(:, 2:end), 2), 1, size(testSet_Original, 2)-1) )./repmat(std(testSet_Original(:, 2:end), 0, 2), 1, size(testSet_Original, 2)-1);
    
    uniqueClasses = unique(testSet_Original(:,1));
    
    trainSet_Averaged = zeros(length(uniqueClasses), size(trainSet_Normed,2));
    testSet_Averaged = zeros(length(uniqueClasses), size(testSet_Normed,2));
    figure
    suptitle( strrep(fileName, '_', '\_') )
    figInd = 1;
    extremes = zeros(2, 1);
    for ind = 1:length(uniqueClasses)
        trainSet_Averaged(ind,:) = sum(trainSet_Normed(trainSet_Normed(:, 1)==uniqueClasses(ind), :), 1)./sum(trainSet_Normed(:, 1)==uniqueClasses(ind));
        testSet_Averaged(ind,:) = sum(testSet_Normed(testSet_Normed(:, 1)==uniqueClasses(ind), :), 1)./sum(testSet_Normed(:, 1)==uniqueClasses(ind));
        extremes(1) = min(extremes(1), min(min(trainSet_Averaged(ind,2:end)), min(testSet_Averaged(ind,2:end))));
        extremes(2) = max(extremes(2), max(max(trainSet_Averaged(ind,2:end)), max(testSet_Averaged(ind,2:end))));
    end
    dlmwrite(strcat(data_Averaged, char(fileName), '_TRAIN'), trainSet_Averaged, 'delimiter', ' ', 'precision', 6);
    dlmwrite(strcat(data_Averaged, char(fileName), '_TEST'), testSet_Averaged, 'delimiter', ' ', 'precision', 6);
    
    dlmwrite(strcat(data_Normed, char(fileName), '_TRAIN'), trainSet_Normed, 'delimiter', ' ', 'precision', 6);
    dlmwrite(strcat(data_Normed, char(fileName), '_TEST'), testSet_Normed, 'delimiter', ' ', 'precision', 6);
    
    for ind = 1:length(uniqueClasses)
        subplot(length(uniqueClasses), 2, figInd)
        plot(trainSet_Averaged(ind,2:end))
        axis tight; grid on; ylim(extremes)
        subplot(length(uniqueClasses), 2, figInd+1)
        plot(testSet_Averaged(ind,2:end))
        axis tight; grid on; ylim(extremes)
        figInd = figInd + 2;
    end
    print(gcf, '-dpng', '-r300', char(strcat(plotsDir, 'TimeSeries/', char(fileName), '.png')));
    close gcf
end
