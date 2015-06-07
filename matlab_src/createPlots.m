close all; clear all; clc;
% Read all the files named below
fileNames = {'synthetic_control'};
% fileNames = {'synthetic_control', 'Gun_Point', 'CBF', 'FaceAll', 'OSULeaf',...
%     'SwedishLeaf', '50words', 'Trace', 'Two_Patterns', 'wafer', 'FaceFour', ...
%     'Lighting2', 'Lighting7', 'ECG200', 'Adiac', 'yoga', 'FISH', 'Plane', ...
%     'Car', 'Beef', 'Coffee', 'OliveOil', 'CinC_ECG_torso', ...
%     'ChlorineConcentration', 'DiatomSizeReduction', 'ECGFiveDays', 'FacesUCR', ...
%     'Haptics', 'InlineSkate', 'ItalyPowerDemand', 'MALLAT', 'MedicalImages', ...
%     'MoteStrain', 'SonyAIBORobotSurfaceII', 'SonyAIBORobotSurface', ...
%     'StarLightCurves', 'Symbols', 'TwoLeadECG', 'WordsSynonyms', 'Cricket_X', ...
%     'Cricket_Y', 'Cricket_Z', 'uWaveGestureLibrary_X', 'uWaveGestureLibrary_Y',...
%     'uWaveGestureLibrary_Z', 'NonInvasiveFatalECG_Thorax1', 'NonInvasiveFatalECG_Thorax2'};

fSz = 12;
restarts = {'0'}; %, 'I', '10'
rankingType = 'exp'; %lin
distType = {'Euclidean', 'Normal', 'Lucky', 'Gaussian', 'Uniform'}; %, 'SkewedNormal'
filePostfix = {'_Accuracy.csv', '_TotalTime.csv', '_RunTime.csv'};
windowSize = [100, 20, 15, 10, 5];
numRuns = 10;

dataRow = 1;
% firstRowData = false; % Set false 
% if(firstRowData)
%     dataRow = 0;
% end
posGaus = find(ismember(distType,'Gaussian'));
posUnif = find(ismember(distType,'Uniform'));
posSkew = find(ismember(distType,'SkewedNormal'));

color_mat = [100,100,100;...  % Euclidean
             255,0,  0;...    % Normal DTW
             0,  0,  0];      % Lucky DTW
if(posUnif)
    color_mat = [color_mat;...
                 0,255,0];    % Uniform
end
if(posGaus)
    color_mat = [color_mat;...
                 0,0,255];    % Gaussian
end
if(posSkew)
    color_mat = [color_mat;...
                 0,255,255]; % SkewedNormal
end

dir.Base    = '../results';
dir.InDet   = '/';

for restart = restarts
    dir.InHeu   = ['/' rankingType '/' char(restart) '/'];
    dir.Out     = ['../plots/' rankingType '/' char(restart) '/'];
    runType = ['_' char(restart)];

    for fileName = fileNames
        fileNameVar = strcat('F_', char(fileName));
        mkdir( char( strcat(dir.Out, fileName) ) );
        for window = windowSize
            windowName = strcat('W_', num2str(window));
            accuracyMat.(windowName) = zeros(length(fileNames), 3+(length(distType)-3)*2);
            errorMat.(windowName) = zeros(length(fileNames), 3+(length(distType)-3)*2);
            rtMat.(windowName) = zeros(length(fileNames), length(distType));
            for type = 1:length(distType)
                if(type==1 || type==2 || type==3)
                    dirString = strcat(dir.Base, dir.InDet);
                elseif(type==4 || type==5 || type==6)
                    dirString = strcat(dir.Base, dir.InHeu);
                end
                if(type==1)
                    fileNameString = strcat(char(fileName), '_0_', distType(type));
                else
                    fileNameString = strcat(char(fileName), '_', num2str(window), '_', distType(type));
                end
                fileTotalTime = strcat(dirString, fileNameString, filePostfix(2));
                totalTime = csvread( char(fileTotalTime));
                fileObtainedResults = strcat(dirString, fileNameString, filePostfix(1));
                obtainedClassification = csvread( char(fileObtainedResults), dataRow);
                if (type==1 || type==2 || type==3)
                    obtainedClassification = sortrows(obtainedClassification, 2);
                    if(type==1)
                        allresults.(fileNameVar).(char(distType(type))).('classification') = obtainedClassification(:,4);
                    else
                        allresults.(fileNameVar).(char(distType(type))).(windowName).('classification') = obtainedClassification(:,4);
                    end
                elseif (type==3 || type==4 || type==5)
                    obtainedClassification = sortrows(obtainedClassification, [1 3]);
                    allresults.(fileNameVar).(char(distType(type))).(windowName).('classification') = obtainedClassification(:,[1,2,5]);
                    totalTime = totalTime/10;
                    fileRunTime = strcat(dirString,char(fileName),'_',num2str(window),'_',distType(type),filePostfix(3));
                    if(dataRow == 0)
                        runTimes = csvread( char(fileRunTime), 0, 1);
                        runTimes = sum(reshape(runTimes, numRuns, size(runTimes, 1)/numRuns), 2);
                    else
                        runTimes = csvread( char(fileRunTime), 1, 1);
                    end
                    allresults.(fileNameVar).(char(distType(type))).(windowName).('runTimes') = runTimes;
                end
                if (type == 2)
                    classes.(fileNameVar).Target = obtainedClassification(:,3);
                end
                if(type==1)
                    allresults.(fileNameVar).(char(distType(type))).('totalTime') = totalTime;
                else
                    allresults.(fileNameVar).(char(distType(type))).(windowName).('totalTime') = totalTime;
                end
            end
        end
    end
    clear dataRow dirString fileNameString fileObtainedResults filePostfix fileRunTime fileTotalTime firstRowData obtainedClassification runTimes totalTime type 
    
%     save(strcat(dir.Base, dir.Out, 'compiled.mat'), 'allresults', 'classes')

    for ind = 1:length(fileNames)
        disp(char(fileNames(ind)));
        % Used for calculating maximum error percentage so we can make all figure y-axes of same height
        accminlim = 100;	accmaxlim = 0;
        rtminlim = 1e12;    rtmaxlim = 0;
        fileNameVar = strcat('F_', char(fileNames(ind)));
        % Calculate the error percentages of the different algos
        % also compile a list of accuracy values of all datasets
        for window = windowSize
            windowName = strcat('W_', num2str(window));
            % Since we have 3 deterministic methods not using multiple runs
            accuracyVals.(windowName) = zeros(numRuns, length(distType)-3);
            errorVals.(windowName) = zeros(numRuns, length(distType)-3);
            for run = 1:numRuns
                runName = strcat('R_', num2str(run));
                if(posUnif)
                    uniformClassification = allresults.(fileNameVar).(char(distType(posUnif))).(windowName).classification;
                    hResults.Uniform.(windowName).(runName) = uniformClassification(uniformClassification(:,1)==run & uniformClassification(:,2)==window, 3);
                    accuracy.Uniform.(windowName).(runName) = 100*sum(classes.(fileNameVar).Target==hResults.Uniform.(windowName).(runName))/length(classes.(fileNameVar).Target);
                    error.Uniform.(windowName).(runName) = (100-accuracy.Uniform.(windowName).(runName))/100;
                    accuracyVals.(windowName)(run, posUnif-3) = accuracy.Uniform.(windowName).(runName);
                    errorVals.(windowName)(run, posUnif-3) = error.Uniform.(windowName).(runName);                
                end
                if(posGaus)
                    gaussianClassification = allresults.(fileNameVar).(char(distType(posGaus))).(windowName).classification;
                    hResults.Gaussian.(windowName).(runName) = gaussianClassification(gaussianClassification(:,1)==run & gaussianClassification(:,2)==window, 3);
                    accuracy.Gaussian.(windowName).(runName) = 100*sum(classes.(fileNameVar).Target==hResults.Gaussian.(windowName).(runName))/length(classes.(fileNameVar).Target);
                    error.Gaussian.(windowName).(runName) = (100-accuracy.Gaussian.(windowName).(runName))/100;
                    accuracyVals.(windowName)(run, posGaus-3) = accuracy.Gaussian.(windowName).(runName);
                    errorVals.(windowName)(run, posGaus-3) = error.Gaussian.(windowName).(runName);
                end
                if(posSkew)
                    skewedClassification = allresults.(fileNameVar).(char(distType(posSkew))).(windowName).classification;
                    hResults.Skewed.(windowName).(runName) = skewedClassification(skewedClassification(:,1)==run & skewedClassification(:,2)==window, 3);
                    accuracy.Skewed.(windowName).(runName) = 100*sum(classes.(fileNameVar).Target==hResults.Skewed.(windowName).(runName))/length(classes.(fileNameVar).Target);
                    error.Skewed.(windowName).(runName) = (100-accuracy.Skewed.(windowName).(runName))/100;
                    accuracyVals.(windowName)(run, posSkew-3) = accuracy.Skewed.(windowName).(runName);
                    errorVals.(windowName)(run, posSkew-3) = error.Skewed.(windowName).(runName);
                end
            end
            accuracy.Euclidean = 100*sum(classes.(fileNameVar).Target==allresults.(fileNameVar).(char(distType(1))).classification)/length(classes.(fileNameVar).Target);
            error.Euclidean = (100-accuracy.Euclidean)/100;
            accuracy.Normal.(windowName) = 100*sum(classes.(fileNameVar).Target==allresults.(fileNameVar).(char(distType(2))).(windowName).classification)/length(classes.(fileNameVar).Target);
            error.Normal.(windowName) = (100-accuracy.Normal.(windowName))/100;
            accuracy.Lucky.(windowName) = 100*sum(classes.(fileNameVar).Target==allresults.(fileNameVar).(char(distType(3))).(windowName).classification)/length(classes.(fileNameVar).Target);
            error.Lucky.(windowName) = (100-accuracy.Lucky.(windowName))/100;
            % compile a set of accuracy values for plotting against each other
            rtMat.(windowName)(ind,1:3) = [allresults.(fileNameVar).Euclidean.totalTime,...
                                           allresults.(fileNameVar).Normal.(windowName).totalTime,...
                                           allresults.(fileNameVar).Lucky.(windowName).totalTime];
            accuracyMat.(windowName)(ind,1:3) = [accuracy.Euclidean,...
                                                 accuracy.Normal.(windowName),...
                                                 accuracy.Lucky.(windowName)];
            errorMat.(windowName)(ind,1:3) = [error.Euclidean,...
                                              error.Normal.(windowName),...
                                              error.Lucky.(windowName)];
            % lenDet+(pos-lenDet)*2 = 3+(4-3)*2=5
            % lenDet+(pos-lenDet)*2 = 3+(5-3)*2=7
            if(posUnif)
                numMeanCol = (3+(posUnif-3)*2)-1;
                numStdCol = (3+(posUnif-3)*2);
                rtMat.(windowName)(ind,posUnif) = mean(allresults.(fileNameVar).Uniform.(windowName).runTimes);
                accuracyMat.(windowName)(ind,[numMeanCol, numStdCol]) = [mean(accuracyVals.(windowName)(:,posUnif-3)),...
                                                                         std(accuracyVals.(windowName)(:,posUnif-3))];
                errorMat.(windowName)(ind,[numMeanCol, numStdCol]) = [mean(errorVals.(windowName)(:,posUnif-3)),...
                                                                      std(errorVals.(windowName)(:,posUnif-3))];
            end
            if(posGaus)
                numMeanCol = (3+(posGaus-3)*2)-1;
                numStdCol = (3+(posGaus-3)*2);
                rtMat.(windowName)(ind,posGaus) = mean(allresults.(fileNameVar).Gaussian.(windowName).runTimes);
                accuracyMat.(windowName)(ind,[numMeanCol, numStdCol]) = [mean(accuracyVals.(windowName)(:,posGaus-3)),...
                                                                         std(accuracyVals.(windowName)(:,posGaus-3))];
                errorMat.(windowName)(ind,[numMeanCol, numStdCol]) = [mean(errorVals.(windowName)(:,posGaus-3)),...
                                                                      std(errorVals.(windowName)(:,posGaus-3))];
            end
            if(posSkew)
                numMeanCol = (3+(posSkew-3)*2)-1;
                numStdCol = (3+(posSkew-3)*2);
                rtMat.(windowName)(ind,posSkew) = mean(allresults.(fileNameVar).SkewedNormal.(windowName).runTimes);
                accuracyMat.(windowName)(ind,[numMeanCol, numStdCol]) = [mean(accuracyVals.(windowName)(:,posSkew-3)),...
                                                                         std(accuracyVals.(windowName)(:,posSkew-3))];
                errorMat.(windowName)(ind,[numMeanCol, numStdCol]) = [mean(errorVals.(windowName)(:,posSkew-3)),...
                                                                      std(errorVals.(windowName)(:,posSkew-3))];
            end
            minAccuracy = min(accuracy.Euclidean, min(accuracy.Normal.(windowName), min(accuracy.Lucky.(windowName), min( accuracyVals.(windowName)(:) ))));
            maxAccuracy = max(accuracy.Euclidean, max(accuracy.Normal.(windowName), max(accuracy.Lucky.(windowName), max( accuracyVals.(windowName)(:) ))));
            if(accminlim>minAccuracy)
                accminlim = floor(minAccuracy);
            end
            if(accmaxlim<maxAccuracy)
                accmaxlim = ceil(maxAccuracy);
            end
            % calculate the maximum runtime
            temp = [allresults.(fileNameVar).Normal.(windowName).totalTime,...%allresults.(fileNameVar).Euclidean.totalTime,...
                    allresults.(fileNameVar).Lucky.(windowName).totalTime];
            if(posUnif)
                temp = [temp, allresults.(fileNameVar).Uniform.(windowName).runTimes'];
            end
            if(posGaus)
                temp = [temp, allresults.(fileNameVar).Gaussian.(windowName).runTimes'];
            end
            if(posSkew)
                temp = [temp, allresults.(fileNameVar).SkewedNormal.(windowName).runTimes'];
            end
            minrt = min(temp);
            maxrt = max(temp);
            if(rtminlim> minrt)
                rtminlim = (minrt);
            end
            if(rtmaxlim<maxrt)
                rtmaxlim = (maxrt);
            end
        end

        % start plotting the accuracy and runtime
        for window = windowSize
            windowName = strcat('W_', num2str(window));
            x = (1:10)';
            acc_Combined = zeros(numRuns, length(distType));
            acc_Combined(:,1) = repmat(accuracy.Euclidean, numRuns, 1);
            acc_Combined(:,2) = repmat(accuracy.Normal.(windowName), numRuns, 1);
            acc_Combined(:,3) = repmat(accuracy.Lucky.(windowName), numRuns, 1);
            rt_Combined = zeros(numRuns, length(distType));
            rt_Combined(:, 1) = repmat(allresults.(fileNameVar).Euclidean.totalTime, numRuns, 1);
            rt_Combined(:, 2) = repmat(allresults.(fileNameVar).Normal.(windowName).totalTime, numRuns, 1);
            rt_Combined(:, 3) = repmat(allresults.(fileNameVar).Lucky.(windowName).totalTime, numRuns, 1);
            if(posUnif)
                acc_Combined(:,posUnif) = accuracyVals.(windowName)(:,posUnif-3);
                rt_Combined(:, posUnif) = allresults.(fileNameVar).Uniform.(windowName).runTimes;
            end
            if(posGaus)
                acc_Combined(:,posGaus) = accuracyVals.(windowName)(:,posGaus-3);
                rt_Combined(:,posGaus) = allresults.(fileNameVar).Gaussian.(windowName).runTimes;
            end
            if(posSkew)
                acc_Combined(:,posSkew) = accuracyVals.(windowName)(:,posSkew-3);
                rt_Combined(:,posSkew) = allresults.(fileNameVar).SkewedNormal.(windowName).runTimes;
            end

            currFigHandle=figure('Units','inches', 'Position',[0 0 8 8]);
            axis tight
            % adjust the paper size to get rid of white space
%             set(gca,'units','inches')
%             pos = get(gca,'Position');
%             ti = get(gca,'TightInset');

%             set(gcf, 'PaperUnits','inches');
%             set(gcf, 'PaperSize', [pos(3)+ti(1)+ti(3) pos(4)+ti(2)+ti(4)]);
%             set(gcf, 'PaperPositionMode', 'manual');
%             set(gcf, 'PaperPosition',[0 0 pos(3)+ti(1)+ti(3) pos(4)+ti(2)+ti(4)]);

            [ax, h1, h2] = plotyy(x, (rt_Combined/allresults.(fileNameVar).Lucky.(windowName).totalTime),...
                                  x, acc_Combined,...
                                  'bar', 'line');
            temp = get(ax(1), 'YLim');
            set(ax(1), 'XLim', [0 11],...      % adjust the runtime x scale
                       'XTick', 1:10,...
                       'YLim', [temp(1) 2*temp(2)-temp(1)],...
                       'YTick', temp(1): (temp(2)-temp(1))/5 :2*temp(2)-temp(1));
            set(ax(2), 'XLim', [0 11],...      % adjust the accuracy x scale
                       'XTick', 1:10,...
                       'YLim', [2*accminlim-accmaxlim accmaxlim],...
                       'YTick', 2*accminlim-accmaxlim:(accmaxlim-accminlim)/5:accmaxlim);
            set(ax(2), 'XGrid', 'on', 'YGrid', 'on')

            % color_mat Defined at the start of script
            cmap = colormap(ax(1), color_mat/255);   % assign colormap to bar graph
            markerSz=4;
            lineWidth=1;
            set(h2(1), 'Marker', '.', 'MarkerSize', markerSz, 'LineStyle', '--', 'LineWidth', lineWidth, 'Color', color_mat(1,:)/255);
            set(h2(2), 'Marker', 's', 'MarkerSize', markerSz, 'LineStyle', '--', 'LineWidth', lineWidth, 'Color', color_mat(2,:)/255);
            set(h2(3), 'Marker', 'd', 'MarkerSize', markerSz, 'LineStyle', '--', 'LineWidth', lineWidth, 'Color', color_mat(3,:)/255);
            if(posUnif)
                set(h2(posUnif), 'Marker', '+', 'MarkerSize', markerSz, 'LineStyle', '--', 'LineWidth', lineWidth, 'Color', color_mat(posUnif,:)/255);
            end
            if(posGaus)
                set(h2(posGaus), 'Marker', '*', 'MarkerSize', markerSz, 'LineStyle', '--', 'LineWidth', lineWidth, 'Color', color_mat(posGaus,:)/255);
            end
            if(posSkew)
                set(h2(posSkew), 'Marker', '^', 'MarkerSize', markerSz, 'LineStyle', '--', 'LineWidth', lineWidth, 'Color', color_mat(posSkew,:)/255);
            end

            title([num2str(window) '% window'], 'FontSize', fSz+1)
            ylabel(ax(1), 'Run Time (Multiples of LTW Run Time)', 'FontSize', fSz) %, 'FontName', fontName
            ylabel(ax(2), 'Accuracy (percentage)', 'FontSize', fSz) %, 'FontName', fontName
            hLegend = legend(ax(2), distType, 'Location', 'best'); %
            legend('boxoff')
%             print(gcf, '-depsc', '-r300', char(strcat(dir.Out, fileNames(ind), '/',windowName, '_Restarts', runType, '.eps')));
            print(gcf, '-dpng', '-r150', char(strcat(dir.Out, fileNames(ind), '/',windowName, '_Restarts', runType, '.png')));
            close gcf
        end
    end

    x = -2:102; y = -2:102;
    temp = zeros(length(fileNames),3);
    xlabelString = {'ED', 'DTW', 'LTW'};
    ylabelstring = {};
    for distNum = 1:length(distType)-3
        ylabelString(distNum) = strcat('RTW-', distType(distNum+3));
    end
    deterministic = [1,2,3];
    tempsum = zeros(1,3);
    for win = windowSize
        if(win == 0)
            continue;
        end
        windowName = strcat('W_', num2str(win));
        % start plotting the errors
        mkdir( strcat(dir.Out, 'Accuracy_', num2str(win)) )
        distNum = 1;
        for heu = 1:2:2*(length(distType)-3)
            for det = 1:length(deterministic)
                figure
                axis tight;
                set(gcf, 'Units','inches', 'Position',[0 0 8 8])
%                 title([char(xlabelString(deterministic(det))) ' vs ' char(ylabelString(heu))], 'FontSize', fSz)
                hold on; grid on; axis square
                xlim([-2 102]);ylim([-2 102]);
                plot(x-2,y, 'Color', [0.8 0.8 0.8], 'LineWidth', 1);
                plot(x+2,y, 'Color', [0.8 0.8 0.8], 'LineWidth', 1);
                set(gca, 'XTick', 0:10:100, 'YTick', 0:10:100);
                temp = [(accuracyMat.(windowName)(:,deterministic(det))-accuracyMat.(windowName)(:,3+heu))<-2,...
                        abs(accuracyMat.(windowName)(:,deterministic(det))-accuracyMat.(windowName)(:,3+heu))<2,...
                        (accuracyMat.(windowName)(:,deterministic(det))-accuracyMat.(windowName)(:,3+heu))>2];
%                 tempsum = [tempsum; sum(temp)];
                scatter(accuracyMat.(windowName)(temp(:,1),deterministic(det)), accuracyMat.(windowName)(temp(:,1),3+heu), 180, '+')
                scatter(accuracyMat.(windowName)(temp(:,3),deterministic(det)), accuracyMat.(windowName)(temp(:,3),3+heu), 180, 'x')
                scatter(accuracyMat.(windowName)(temp(:,2),deterministic(det)), accuracyMat.(windowName)(temp(:,2),3+heu), 180, '.')
                xlabel(char(xlabelString(det)), 'FontSize', fSz) %, 'FontName', fontName
                ylabel(char(ylabelString(distNum)), 'FontSize', fSz) %, 'FontName', fontName

%                 print(gcf, '-depsc', '-r300', char(strcat(dir.Out, 'Accuracy_', num2str(win), '/', char(ylabelString(heu)), 'Vs', char(xlabelString(det)), '.eps')));
                print(gcf, '-dpng', '-r150', char(strcat(dir.Out, 'Accuracy_', num2str(win), '/', char(ylabelString(distNum)), 'Vs', char(xlabelString(det)), '.png')));
                close gcf
            end
            distNum = distNum+1;
        end
    end
end
