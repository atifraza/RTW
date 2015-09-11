close all; clear all; clc;
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

origDataDir = '../data/Original/';
normDataDir = '../data/Normalized/';
plotsDir = '../data/Plots/Original_vs_Normalized/';
mean_stddev_all = zeros(length(fileNames),4);
for ind = 1:length(fileNames)
    origTrnTS = sortrows(load(strcat(origDataDir, char(fileNames(ind)), '_TRAIN')));
    origTstTS = sortrows(load(strcat(origDataDir, char(fileNames(ind)), '_TEST')));
    avgdTrnTS = sortrows(load(strcat(normDataDir, char(fileNames(ind)), '_TRAIN')));
    avgdTstTS = sortrows(load(strcat(normDataDir, char(fileNames(ind)), '_TEST')));

    uniqueClasses = unique(origTrnTS(:,1));
    figure
    suptitle( strrep(fileNames(ind), '_', '\_') )
    clrInd = 1;
    clr = colormap(lines(length(uniqueClasses)));
    if(length(uniqueClasses)<8)
        subFigInd=0;
        for class = 1:length(uniqueClasses)
            subplot(length(uniqueClasses), 4, 1+subFigInd)
            temp=origTrnTS(origTrnTS(:, 1)==uniqueClasses(class), 2:end);
            h=plot(temp');
            axis tight; grid on; hold on;
            ylabel(['Class: ' num2str(uniqueClasses(class))])
            xlabel('Original Training Set')
            set(h, 'Color', clr(clrInd, :))

            subplot(length(uniqueClasses), 4, 2+subFigInd)
            temp = avgdTrnTS(avgdTrnTS(:, 1)==uniqueClasses(class), 2:end);
            h=plot(temp');
            axis tight; grid on; hold on;
            ylabel(['Class: ' num2str(uniqueClasses(class))])
            xlabel('Normalized Training Set')
            set(h, 'Color', clr(clrInd, :))

            subplot(length(uniqueClasses), 4, 3+subFigInd)
            temp=origTstTS(origTstTS(:, 1)==uniqueClasses(class), 2:end);
            h=plot(temp');
            axis tight; grid on; hold on;
            ylabel(['Class: ' num2str(uniqueClasses(class))])
            xlabel('Original Testing Set')
            set(h, 'Color', clr(clrInd, :))

            subplot(length(uniqueClasses), 4, 4+subFigInd)
            temp = avgdTstTS(avgdTstTS(:, 1)==uniqueClasses(class), 2:end);
            h=plot(temp');
            axis tight; grid on; hold on;
            ylabel(['Class: ' num2str(uniqueClasses(class))])
            xlabel('Normalized Testing Set')
            set(h, 'Color', clr(clrInd, :))

            subFigInd = subFigInd+4;
            clrInd = clrInd + 1;
        end
    else
        for class = 1:length(uniqueClasses)
            subplot(2, 2, 1)
            temp=origTrnTS(origTrnTS(:, 1)==uniqueClasses(class), 2:end);
            h=plot(temp');
            axis tight; grid on; hold on;
            ylabel('All Class')
            xlabel('Original Training Set')
            set(h, 'Color', clr(clrInd, :))

            subplot(2, 2, 2)
            temp = avgdTrnTS(avgdTrnTS(:, 1)==uniqueClasses(class), 2:end);
            h=plot(temp');
            axis tight; grid on; hold on;
            ylabel('All Class')
            xlabel('Normalized Training Set')
            set(h, 'Color', clr(clrInd, :))

            subplot(2, 2, 3)
            temp=origTstTS(origTstTS(:, 1)==uniqueClasses(class), 2:end);
            h=plot(temp');
            axis tight; grid on; hold on;
            ylabel('All Class')
            xlabel('Original Testing Set')
            set(h, 'Color', clr(clrInd, :))

            subplot(2, 2, 4)
            temp = avgdTstTS(avgdTstTS(:, 1)==uniqueClasses(class), 2:end);
            h=plot(temp');
            axis tight; grid on; hold on;
            ylabel('All Class')
            xlabel('Normalized Testing Set')
            set(h, 'Color', clr(clrInd, :))

            clrInd = clrInd + 1;
        end
    end
    print(gcf, '-dpng', '-r300', char(strcat(plotsDir, char(fileNames(ind)), '.png')));
    close gcf
end
