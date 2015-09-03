close all; clear all; clc
% Read all the files named below
fileNames = {'Beef'};
% fileNames = {'synthetic_control', 'Gun_Point', 'CBF', 'FaceAll', 'OSULeaf',...
%     'SwedishLeaf', '50words', 'Trace', 'Two_Patterns', 'wafer', 'FaceFour', ...
%     'Lighting2', 'Lighting7', 'ECG200', 'Adiac', 'yoga', 'FISH', 'Plane', ...
%     'Car', 'Beef', 'Coffee', 'OliveOil', 'CinC_ECG_torso', ...
%     'ChlorineConcentration', 'DiatomSizeReduction', 'ECGFiveDays', 'FacesUCR', ...
%     'Haptics', 'InlineSkate', 'ItalyPowerDemand', 'MALLAT', 'MedicalImages', ...
%     'MoteStrain', 'SonyAIBORobotSurfaceII', 'SonyAIBORobotSurface', ...
%     'StarLightCurves', 'Symbols', 'TwoLeadECG', 'WordsSynonyms', 'Cricket_X', ...
%     'Cricket_Y', 'Cricket_Z', 'uWaveGestureLibrary_X', 'uWaveGestureLibrary_Y', ...
%     'uWaveGestureLibrary_Z', 'NonInvasiveFatalECG_Thorax1', 'NonInvasiveFatalECG_Thorax2'};

% Aesthetic settings
fSz = 12;   fName = 'Times';    mrkrSz=7;   lnWd=2;

% Experimental settings used
restarts = {'0'}; %, 'I' ,'10' 
rankingType = 'exp'; %lin
windowSize = [100, 20, 15, 10, 5]; %[1:20, 30:10:100]
numDetDist = 3;
dist = [...
    struct('long', 'Euclidean',  'marker', 'p',  'color', [228, 26, 28], 'short', 'ED'),...
    struct('long', 'Normal',     'marker', 'h',  'color', [ 55,126,184], 'short', 'DTW'),...
    struct('long', 'Lucky',      'marker', 's',  'color', [ 77,175, 74], 'short', 'LTW'),...
    struct('long', 'Uniform',    'marker', '+',  'color', [152, 78,163], 'short', 'RTW-Uniform'),...
    struct('long', 'Gaussian',   'marker', '+',  'color', [255,127,  0], 'short', 'RTW-Gaussian'),...
%     struct('long', 'Gaussian_Manhattan',    'marker', '+',  'color', [255,255, 51], 'short', 'RTW-GaussMan'),...
    ];
names = cell(1,length(dist));
for typeInd =1:length(dist)
    names(typeInd) = cellstr(dist(typeInd).short);
end
color_mat = reshape([dist(:).color],3,length(dist))';

numRuns = 10;
dataRow = 1;
% firstRowData = false; % Set false 
% if(firstRowData)
%     dataRow = 0;
% end

dir.Base    = '../results';
dir.InDet   = '/';
filePostfix = {'_Accuracy.csv', '_TotalTime.csv', '_RunTime.csv'};

for restart = restarts
    dir.InHeu   = ['/' rankingType '/' char(restart) '/'];
    dir.Out     = ['../plots/' rankingType '/' char(restart) '/'];
    runType = ['_' char(restart)];

    for fileName = fileNames
        fileNameVar = strcat('F_', char(fileName));
        [~, ~, ~] = mkdir( char( strcat(dir.Out, fileName) ) );
        
        for window = windowSize
            windowName = strcat('W_', num2str(window));
            % Unsimplified expression for number of columns: numDetDist+(length(dist)-numDetDist)*2
            errAll.(fileNameVar) = zeros(100, 2*length(dist)-numDetDist);
            accuracyMat.(windowName) = zeros(length(fileNames), 2*length(dist)-numDetDist);
            runTimesMat.(windowName) = zeros(length(fileNames), length(dist));
            
            for typeInd = 1:length(dist)
                if(typeInd<=numDetDist)
                    dirString = strcat(dir.Base, dir.InDet);
                else    %if(typeInd>numDetDist)
                    dirString = strcat(dir.Base, dir.InHeu);
                end
                if(strcmp(dist(typeInd).long, 'Euclidean'))
                    fileNameString = strcat(char(fileName), '_0_', dist(typeInd).long);
                else
                    fileNameString = strcat(char(fileName), '_', num2str(window), '_', dist(typeInd).long);
                end
                fileTotalTime = strcat(dirString, fileNameString, filePostfix(2));
                totalTime = csvread( char(fileTotalTime));
                fileObtainedResults = strcat(dirString, fileNameString, filePostfix(1));
                obtainedClassification = csvread( char(fileObtainedResults), dataRow);
                if (typeInd<=numDetDist)
                    obtainedClassification = sortrows(obtainedClassification, 2);
                    if(strcmp(dist(typeInd).long, 'Euclidean'))
                        allresults.(fileNameVar).(char(dist(typeInd).long)).('classification') = obtainedClassification(:,4);
                    else
                        allresults.(fileNameVar).(char(dist(typeInd).long)).(windowName).('classification') = obtainedClassification(:,4);
                    end
                else    %if (typeInd==3 || typeInd==4 || typeInd==5)
                    obtainedClassification = sortrows(obtainedClassification, [1 3]);
                    allresults.(fileNameVar).(char(dist(typeInd).long)).(windowName).('classification') = obtainedClassification(:,[1,2,5]);
                    totalTime = totalTime/10;
                    fileRunTime = strcat(dirString,char(fileName),'_',num2str(window),'_',dist(typeInd).long,filePostfix(3));
                    if(dataRow == 0)
                        runTimes = csvread( char(fileRunTime), 0, 1);
                        runTimes = sum(reshape(runTimes, numRuns, size(runTimes, 1)/numRuns), 2);
                    else
                        runTimes = csvread( char(fileRunTime), 1, 1);
                    end
                    allresults.(fileNameVar).(char(dist(typeInd).long)).(windowName).('runTimes') = runTimes;
                end
                if (strcmp(dist(typeInd).long, 'Normal'))
                    classes.(fileNameVar).Target = obtainedClassification(:,3);
                end
                if(strcmp(dist(typeInd).long, 'Euclidean'))
                    allresults.(fileNameVar).(char(dist(typeInd).long)).('totalTime') = totalTime;
                else
                    allresults.(fileNameVar).(char(dist(typeInd).long)).(windowName).('totalTime') = totalTime;
                end
            end
        end
    end

    for ind = 1:length(fileNames)
        disp(char(fileNames(ind)));
        % Used for calculating maximum error percentage so we can make all figure y-axes of same height
        accMinLim = 100;    accMaxLim = 0;
        rtMinLim = realmax; rtMaxLim = -realmin;
        fileNameVar = strcat('F_', char(fileNames(ind)));
        % Calculate the error percentages of the different algos
        % also compile a list of accuracy values of all datasets
        
        for window = windowSize
            windowName = strcat('W_', num2str(window));
            % Since we have 3 deterministic methods not using multiple runs
            acc_PerRun.(windowName) = zeros(numRuns, length(dist)-numDetDist);
            err_PerRun.(windowName) = zeros(numRuns, length(dist)-numDetDist);
            
            % compile a set of accuracy values for plotting against each other
            for typeInd = 1:numDetDist
                if(strcmp(dist(typeInd).long, 'Euclidean'))
                    acc_CurrFile.(char(dist(typeInd).long)) = 100*sum(classes.(fileNameVar).Target==allresults.(fileNameVar).(char(dist(typeInd).long)).classification)/length(classes.(fileNameVar).Target);
                    accuracyMat.(windowName)(ind, typeInd) = acc_CurrFile.(char(dist(typeInd).long));
                    runTimesMat.(windowName)(ind, typeInd) = allresults.(fileNameVar).(char(dist(typeInd).long)).totalTime;
                else
                    acc_CurrFile.(char(dist(typeInd).long)).(windowName) = 100*sum(classes.(fileNameVar).Target==allresults.(fileNameVar).(char(dist(typeInd).long)).(windowName).classification)/length(classes.(fileNameVar).Target);
                    accuracyMat.(windowName)(ind, typeInd) = acc_CurrFile.(char(dist(typeInd).long)).(windowName);
                    runTimesMat.(windowName)(ind, typeInd) = allresults.(fileNameVar).(char(dist(typeInd).long)).(windowName).totalTime;
                end
                
            end
            errAll.(fileNameVar)(window, 1:numDetDist) = (100-accuracyMat.(windowName)(ind,1:numDetDist))/100;
            
            for typeInd = numDetDist+1:length(dist)
                for run = 1:numRuns
                    runName = strcat('R_', num2str(run));
                    classification = allresults.(fileNameVar).(char(dist(typeInd).long)).(windowName).classification;
                    hResults.(char(dist(typeInd).long)).(windowName).(runName) = classification(classification(:,1)==run & classification(:,2)==window, 3);
                    acc_CurrFile.(char(dist(typeInd).long)).(windowName).(runName) = 100*sum(classes.(fileNameVar).Target==hResults.(char(dist(typeInd).long)).(windowName).(runName))/length(classes.(fileNameVar).Target);
                    err_CurrFile.(char(dist(typeInd).long)).(windowName).(runName) = (100-acc_CurrFile.(char(dist(typeInd).long)).(windowName).(runName))/100;
                    acc_PerRun.(windowName)(run, typeInd-numDetDist) = acc_CurrFile.(char(dist(typeInd).long)).(windowName).(runName);
                    err_PerRun.(windowName)(run, typeInd-numDetDist) = err_CurrFile.(char(dist(typeInd).long)).(windowName).(runName);
                end
                % lenDet+(pos-lenDet)*2 = 3+(4-3)*2=5
                % lenDet+(pos-lenDet)*2 = 3+(5-3)*2=7
                numMeanCol = (numDetDist+(typeInd-numDetDist)*2)-1;
                numStdCol = (numDetDist+(typeInd-numDetDist)*2);
                runTimesMat.(windowName)(ind, typeInd) = mean(allresults.(fileNameVar).(char(dist(typeInd).long)).(windowName).runTimes);
                errAll.(fileNameVar)(window,[numMeanCol, numStdCol]) = [mean(err_PerRun.(windowName)(:, typeInd-numDetDist)),...
                                                                        std(err_PerRun.(windowName)(:, typeInd-numDetDist))];
                accuracyMat.(windowName)(ind,[numMeanCol, numStdCol]) = [mean(acc_PerRun.(windowName)(:, typeInd-numDetDist)),...
                                                                         std(acc_PerRun.(windowName)(:, typeInd-numDetDist))];
            end
            
            minAccuracy = min( acc_PerRun.(windowName)(:) );
            maxAccuracy = max( acc_PerRun.(windowName)(:) );
            for typeInd = 1:numDetDist
                if(strcmp((char(dist(typeInd).long)), 'Euclidean'))
                    minAccuracy = min(minAccuracy, acc_CurrFile.(char(dist(typeInd).long)));
                    maxAccuracy = max(maxAccuracy, acc_CurrFile.(char(dist(typeInd).long)));
                else
                    minAccuracy = min(minAccuracy, acc_CurrFile.(char(dist(typeInd).long)).(windowName));
                    maxAccuracy = max(maxAccuracy, acc_CurrFile.(char(dist(typeInd).long)).(windowName));
                end
            end

            if(accMinLim>minAccuracy)
                accMinLim = floor(minAccuracy);
            end
            if(accMaxLim<maxAccuracy)
                accMaxLim = ceil(maxAccuracy);
            end
            % calculate the maximum runtime
            temp = zeros(1, numDetDist);
            for typeInd = 1:length(dist)
                if(strcmp((char(dist(typeInd).long)), 'Euclidean'))
                    temp(typeInd) = allresults.(fileNameVar).(char(dist(typeInd).long)).totalTime;
                elseif(typeInd<=numDetDist)
                    temp(typeInd) = allresults.(fileNameVar).(char(dist(typeInd).long)).(windowName).totalTime;
                else
                    temp = [temp, allresults.(fileNameVar).(char(dist(typeInd).long)).(windowName).runTimes'];
                end
            end
            minrt = min(temp);
            maxrt = max(temp);
            if(rtMinLim> minrt)
                rtMinLim = (minrt);
            end
            if(rtMaxLim<maxrt)
                rtMaxLim = (maxrt);
            end
        end
        currFigHandle=figure('Units','inches', 'Position',[0 0 8 8]);
        hold on
        h = zeros(1,length(dist));
        for typeInd = 1:numDetDist
            h(typeInd) = plot(windowSize, errAll.(fileNameVar)(windowSize,typeInd));
            set(h(typeInd), 'Marker', char(dist(typeInd).marker), ...
                            'MarkerSize', mrkrSz, ...
                            'LineStyle', '--', ...
                            'LineWidth', lnWd, ...
                            'Color', color_mat(typeInd,:)/255);
        end
        for typeInd = numDetDist+1:length(dist)
            h(typeInd) = plot(windowSize, errAll.(fileNameVar)(windowSize,(2*typeInd-numDetDist)-1));
            set(h(typeInd), 'Marker', char(dist(typeInd).marker), ...
                            'MarkerSize', mrkrSz, ...
                            'LineStyle', '--', ...
                            'LineWidth', lnWd, ...
                            'Color', color_mat(typeInd,:)/255);
        end
        set(gca, 'XGrid', 'on', 'XMinorGrid', 'on')
        title(strcat(char(fileNames(ind)), ' - Error plot with increasing window size'), 'Interpreter', 'none', 'FontSize', fSz+4)
        xlabel('Window size', 'FontSize', fSz, 'FontName', fName);
        ylabel('Error - Lower is better', 'FontSize', fSz, 'FontName', fName);
        hLegend = legend(h, names, 'Location', 'SouthOutside', 'Orientation', 'horizontal', 'Color', 'none');
        print(gcf, '-dpng', '-r0', char(strcat(dir.Out, '/', fileNames(ind), '/ErrorCurve.png')));
        close gcf
        
        % start plotting the accuracy and runtime
        for window = windowSize
            windowName = strcat('W_', num2str(window));
            x = (1:10)';
            acc_Combined = zeros(numRuns, length(dist));
            rt_Combined = zeros(numRuns, length(dist));
            for typeInd = 1:numDetDist
                if(strcmp((char(dist(typeInd).long)), 'Euclidean'))
                    acc_Combined(:,typeInd) = repmat(acc_CurrFile.(char(dist(typeInd).long)), numRuns, 1);
                    rt_Combined(:, typeInd) = repmat(allresults.(fileNameVar).(char(dist(typeInd).long)).totalTime, numRuns, 1);
                else
                    acc_Combined(:,typeInd) = repmat(acc_CurrFile.(char(dist(typeInd).long)).(windowName), numRuns, 1);
                    rt_Combined(:, typeInd) = repmat(allresults.(fileNameVar).(char(dist(typeInd).long)).(windowName).totalTime, numRuns, 1);
                end
            end
            
            for typeInd = numDetDist+1:length(dist)
                acc_Combined(:,typeInd) = acc_PerRun.(windowName)(:,typeInd-numDetDist);
                rt_Combined(:, typeInd) = allresults.(fileNameVar).(char(dist(typeInd).long)).(windowName).runTimes;
            end

            currFigHandle=figure('Units','inches', 'Position',[0 0 8 8]);
            axis tight
            [ax, h1, h2] = plotyy(x, (rt_Combined),...%/allresults.(fileNameVar).Lucky.(windowName).totalTime),...
                                  x, acc_Combined,...
                                  'bar', 'line');
            temp = get(ax(1), 'YLim');
            set(ax(1), 'XLim', [0 11],...      % adjust the runtime x scale
                       'XTick', 1:10,...
                       'YLim', [temp(1) 2*temp(2)-temp(1)],...
                       'YTick', temp(1): (temp(2)-temp(1))/5 :2*temp(2)-temp(1));
            set(ax(2), 'XLim', [0 11],...      % adjust the accuracy x scale
                       'XTick', 1:10,...
                       'YLim', [2*accMinLim-accMaxLim accMaxLim],...
                       'YTick', 2*accMinLim-accMaxLim:(accMaxLim-accMinLim)/5:accMaxLim);
            set(ax(2), 'XGrid', 'on', 'YGrid', 'on')

            cmap = colormap(ax(1), color_mat/255);   % assign colormap to bar graph
            for typeInd = 1:numDetDist
                set(h2(typeInd), 'Marker', char(dist(typeInd).marker), ...
                                 'MarkerSize', mrkrSz, ...
                                 'LineStyle', '--', ...
                                 'LineWidth', lnWd, ...
                                 'Color', color_mat(typeInd,:)/255);
            end
            for typeInd = numDetDist+1:length(dist)
                set(h2(typeInd), 'Marker', char(dist(typeInd).marker), ...
                                 'MarkerSize', mrkrSz, ...
                                 'LineStyle', '--', ...
                                 'LineWidth', lnWd, ...
                                 'Color', color_mat(typeInd,:)/255);
            end

            title({strcat(char(fileNames(ind)), ' - Window size: ', num2str(window), '%');''}, 'Interpreter', 'none', 'FontSize', fSz+4)
%             ylabel(ax(1), 'Run Time (Multiples of LTW Run Time)', 'FontSize', fSz, 'FontName', fName)
            ylabel(ax(1), 'Run Time (seconds)', 'FontSize', fSz, 'FontName', fName)
            ylabel(ax(2), 'Accuracy (percentage)', 'FontSize', fSz, 'FontName', fName)
            hLegend = legend(ax(2), names, 'Location', 'NorthOutside', 'Orientation', 'horizontal', 'Color', 'none');
%             print(gcf, '-depsc', '-r300', char(strcat(dir.Out, fileNames(ind), '/',windowName, '_Restarts', runType, '.eps')));
            print(gcf, '-dpng', '-r0', char(strcat(dir.Out, fileNames(ind), '/',windowName, '_Restarts', runType, '.png')));
            close gcf
        end
    end

    x = -2:102; y = -2:102;
    temp = zeros(length(fileNames),3);
    xlabelString = names(1:numDetDist);
    ylabelString = names(numDetDist+1:end);
    deterministic = 1:numDetDist;
    tempsum = zeros(1,3);
    for window = windowSize
        if(window == 0)
            continue;
        end
        windowName = strcat('W_', num2str(window));
        % start plotting the errors
        [~, ~, ~] = mkdir( strcat(dir.Out, 'Accuracy/', num2str(window)) );
        [~, ~, ~] = mkdir( strcat(dir.Out, 'Ratios/', num2str(window)) );
        distNum = 1;
        for heu = 1:2:2*(length(dist)-numDetDist)
            for det = 1:length(deterministic)
                figure('Units','inches', 'Position',[0 0 8 8]);
                axis tight;
                hold on; grid on; axis square
                xlim([-2 102]);ylim([-2 102]);
                plot(x-2,y, 'Color', [0.8 0.8 0.8], 'LineWidth', 1);
                plot(x+2,y, 'Color', [0.8 0.8 0.8], 'LineWidth', 1);
                set(gca, 'XTick', 0:10:100, 'YTick', 0:10:100);
                temp = [(accuracyMat.(windowName)(:,deterministic(det))-accuracyMat.(windowName)(:,numDetDist+heu))<-2,...
                        abs(accuracyMat.(windowName)(:,deterministic(det))-accuracyMat.(windowName)(:,numDetDist+heu))<2,...
                        (accuracyMat.(windowName)(:,deterministic(det))-accuracyMat.(windowName)(:,numDetDist+heu))>2];
%                 tempsum = [tempsum; sum(temp)];
                scatter(accuracyMat.(windowName)(temp(:,1),deterministic(det)), accuracyMat.(windowName)(temp(:,1),numDetDist+heu), 180, '+');
                scatter(accuracyMat.(windowName)(temp(:,3),deterministic(det)), accuracyMat.(windowName)(temp(:,3),numDetDist+heu), 180, 'x');
                scatter(accuracyMat.(windowName)(temp(:,2),deterministic(det)), accuracyMat.(windowName)(temp(:,2),numDetDist+heu), 180, '.');
                title(strcat('Accuracy-Accuracy plot - Window size: ', num2str(window), '%'), 'FontSize', fSz+1);
                xlabel(char(xlabelString(det)), 'FontSize', fSz, 'FontName', fName);
                ylabel(char(ylabelString(distNum)), 'FontSize', fSz, 'FontName', fName);

%                 print(gcf, '-depsc', '-r300', char(strcat(dir.Out, 'Accuracy/', num2str(window), '/', char(ylabelString(heu)), 'Vs', char(xlabelString(det)), '.eps')));
                print(gcf, '-dpng', '-r0', char(strcat(dir.Out, 'Accuracy/', num2str(window), '/', char(ylabelString(distNum)), 'Vs', char(xlabelString(det)), '.png')));
                close gcf
                % Plot a ratio by ratio plot for accuracy and runtime
                figure('Units','inches', 'Position',[0 0 4 4]);
                axis tight;
                maxAcc = max([accuracyMat.(windowName)(:,deterministic(det)),accuracyMat.(windowName)(:,numDetDist+heu)],[],2);
                maxRT = max([runTimesMat.(windowName)(:,deterministic(det)),runTimesMat.(windowName)(:,numDetDist+distNum)],[],2);
                accRatio = 1+((accuracyMat.(windowName)(:,numDetDist+heu)-accuracyMat.(windowName)(:,deterministic(det)))./maxAcc);
                min(accRatio);
                max(accRatio);
                rtRatio = 1+((runTimesMat.(windowName)(:,numDetDist+distNum)-runTimesMat.(windowName)(:,deterministic(det)))./maxRT);
                min(rtRatio);
                max(rtRatio);
                scatter(accRatio,rtRatio,180,'.');
                get(gca,'TightInset');
                hold on;
                title(strcat('Ratio of Accuracy and Runtime between ', ylabelString(distNum), ' and ', xlabelString(det), ' - Window size: ', num2str(window), '%'), 'FontSize', fSz+1);
                ylabel(strcat('Runtime (',char(ylabelString(distNum)),'/',char(xlabelString(det)),')'),'FontSize',fSz, 'FontName', fName);
                xlabel(strcat('Accuracy (',char(ylabelString(distNum)),'/',char(xlabelString(det)),')'),'FontSize',fSz, 'FontName', fName);
                set(gca, 'XGrid','on', 'XMinorGrid', 'off', 'YGrid','on', 'YMinorGrid', 'off','XLim',[0 2], 'YLim', [0 2]);
                plot(0:2,[1 1 1], 'Color', [0 0 0], 'LineWidth', 2);
                plot([1 1 1],0:2, 'Color', [0 0 0], 'LineWidth', 2);
                
%                 print(gcf, '-depsc', '-r300', char(strcat(dir.Out, 'Ratios/', num2str(window), '/', char(ylabelString(heu)), 'Vs', char(xlabelString(det)), '.eps')));
                print(gcf, '-dpng', '-r0', char(strcat(dir.Out, 'Ratios/', num2str(window), '/', char(ylabelString(distNum)), 'Vs', char(xlabelString(det)), '.png')));
                close gcf
            end
            distNum = distNum+1;
        end
    end
end
