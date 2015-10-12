close all; clear all; clc
% Read all the files named below
% fileNames = {'Beef'};
% fileNames = {'50words', 'Car', 'CBF', 'CinC_ECG_torso', 'Cricket_X', 'Cricket_Y', 'Cricket_Z', ...
%              'FaceAll', 'FaceFour', 'FacesUCR', 'FISH', 'Gun_Point', 'Haptics', 'InlineSkate', ...
%              'Lighting2', 'Lighting7', 'MedicalImages', 'OSULeaf', 'Plane', 'SwedishLeaf', ...
%              'Symbols', 'synthetic_control', 'Trace', 'Two_Patterns', ...
%              'wafer', 'WordsSynonyms', 'yoga'};

fileNames = {'50words', 'Adiac', 'Beef', 'Car', 'CBF', 'ChlorineConcentration', 'CinC_ECG_torso',...
             'Coffee', 'Cricket_X', 'Cricket_Y', 'Cricket_Z', 'DiatomSizeReduction', 'ECG200',   ...
             'ECGFiveDays', 'FaceAll', 'FaceFour', 'FacesUCR', 'FISH', 'Gun_Point', 'Haptics',   ...
             'InlineSkate', 'ItalyPowerDemand', 'Lighting2', 'Lighting7', 'MALLAT',              ...
             'MedicalImages', 'MoteStrain', 'OliveOil', 'OSULeaf', 'Plane', 'SonyAIBORobotSurface',...
             'SonyAIBORobotSurfaceII', 'SwedishLeaf', 'Symbols', 'synthetic_control', 'Trace',   ...
             'Two_Patterns', 'TwoLeadECG', ...'uWaveGestureLibrary_X', 'uWaveGestureLibrary_Y', 
             ...'uWaveGestureLibrary_Z', 
             'wafer', 'WordsSynonyms', 'yoga'};

% Aesthetic settings
fSz = 14;   fName = 'Times';    mrkrSz=7;   lnWd=2;

% Printing settings
format='-dpng'; % -depsc
ext   ='.png';  % .eps
res='-r0'; % -r300
plotMinClassDiffs = 0;

% Experimental settings used
restarts = {'0'}; %, 'I' ,'10' 
rankingType = 'exp'; %lin
windowSize = [100, 20, 15, 10, 5]; %[1:20, 30:10:100, -2]
numDetDist = 3;
dist = [...
    struct('marker', '.',  'color', [228, 26, 28], 'long', 'Euclidean',  'short', 'ED'),... 'p'
    struct('marker', '.',  'color', [ 55,126,184], 'long', 'Normal',     'short', 'DTW'),... 'h'
    struct('marker', '.',  'color', [ 77,175, 74], 'long', 'Lucky',      'short', 'LTW'),... 's'
    struct('marker', '.',  'color', [152, 78,163], 'long', 'Gaussian',   'short', 'RTW-G EucDist'),... '*'
    struct('marker', '.',  'color', [255,127,  0], 'long', 'Gaussian_Manhattan',    'short', 'RTW-G ManDist'),...'*.'
%     struct('marker', '.',  'color', [255,255, 51], 'long', 'Uniform',    'short', 'RTW-U EucDist'),... '+'
%     struct('marker', '.',  'color', [166, 86, 40], 'long', 'Uniform_Manhattan',    'short', 'RTW-U ManDist'),... '+.'
%     struct('marker', '.',  'color', [247,129,191], 'long', 'SkewedNormal',    'short', 'RTW-SN EucDist'),... 'x'
%     struct('marker', '.',  'color', [153,153,153], 'long', 'SkewedNormal_Manhattan',    'short', 'RTW-SN ManDist'),... 'x.'
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

dir.Base    = '../results/win-size_0-100/';
dir.InDet   = '/';
filePostfix = {'_Accuracy.csv', '_TotalTime.csv', '_RunTime.csv'};

for restart = restarts
    dir.InHeu   = ['/' rankingType '/' char(restart) '/'];
    dir.Out     = ['../plots/' rankingType '/' char(restart) '/'];
    runType = ['_' char(restart)];
    classes = {};

    for fileName = fileNames
        fileNameVar = strcat('F_', char(fileName));
        [~, ~, ~] = mkdir( char( strcat(dir.Out, fileName) ) );
        
        for window = windowSize
            if(window == -2)
                windowName = strcat('W_', 'Irregular');
                errIrWin.(fileNameVar) = zeros(1, 2*(length(dist)-numDetDist));
                accuracyMat.(windowName) = zeros(length(fileNames), 2*(length(dist)-numDetDist));
                runTimesMat.(windowName) = zeros(length(fileNames), length(dist)-numDetDist);
                for typeInd = numDetDist+1:length(dist)
                    dirString = strcat(dir.Base, char(fileName), dir.InHeu);
                    fileNameString = strcat(char(fileName), '_', num2str(window), '_', dist(typeInd).long);
                    fileTotalTime = strcat(dirString, fileNameString, filePostfix(2));
                    totalTime = csvread( char(fileTotalTime));
                    fileObtainedResults = strcat(dirString, fileNameString, filePostfix(1));
                    obtainedClassification = csvread( char(fileObtainedResults), dataRow);
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
                    allresults.(fileNameVar).(char(dist(typeInd).long)).(windowName).('totalTime') = totalTime;
                end
            else
                windowName = strcat('W_', num2str(window));
                % Unsimplified expression for number of columns: numDetDist+(length(dist)-numDetDist)*2
                errAll.(fileNameVar) = zeros(100, 2*length(dist)-numDetDist);
                accuracyMat.(windowName) = zeros(length(fileNames), 2*length(dist)-numDetDist);
                runTimesMat.(windowName) = zeros(length(fileNames), length(dist));

                for typeInd = 1:length(dist)
                    if(typeInd<=numDetDist)
                        dirString = strcat(dir.Base, char(fileName), dir.InDet);
                    else    %if(typeInd>numDetDist)
                        dirString = strcat(dir.Base, char(fileName), dir.InHeu);
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
    %%%%%%%%%%%%%%%%%%% Inter Class Difference Plotting
    %                     if(plotMinClassDiffs)
    %                         isClassCorrect = obtainedClassification(:,4)==obtainedClassification(:,5);
    %                         obtainedClassification = [obtainedClassification, isClassCorrect];
    %                         uniqueClasses = unique(obtainedClassification(:,4));
    %                         counts = zeros(length(uniqueClasses));
    %                         for cls_Ind = 1:length(uniqueClasses)
    %                             currClass = uniqueClasses(cls_Ind);
    %                             currClassData = obtainedClassification(obtainedClassification(:,4)==currClass,:);
    %                             for col = 1:length(uniqueClasses)
    %                                 counts(cls_Ind,col) = sum(currClassData(:,5)==uniqueClasses(col));
    %     %                             if(ind==col)
    %     %                                 counts(ind,col) = sum(currClassData(:,5)==currClass);
    %     %                             else
    %     %                                 counts(ind,col) = sum(currClassData(:,5)~=currClass);
    %     %                             end
    %                             end
    %     %                         counts(ind,:) = [sum(currClassData(:,5)==currClass), sum(currClassData(:,5)~=currClass)];
    %                         end
    %                         [~,centers] = hist(obtainedClassification(:, 6:size(obtainedClassification,2)-1),length(uniqueClasses));
    %                         bar(centers,counts, 0.5,'stacked')
    %                         ax = gca;
    %                         xlabel('Class Distance Delta Centers')
    %                         ylabel('Number of Instances Classified')
    %                         temp = get(ax, 'YLim')
    %                         ylim([temp(1) temp(2)*1.2])
    %                         legend(num2str(uniqueClasses),'Location', 'eastoutside')
    %                         print(gcf, format, res, char(strcat(dir.Out, '/', char(fileName), ' - InterClassDistances', ext)));
    %                         close gcf
    %                     end
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
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
                    if (~isfield(classes, fileNameVar)) %strfind(dist(typeInd).long, 'Normal') ~exist('classes', 'var') 
                        if(typeInd<=numDetDist)
                            classes.(fileNameVar).Target = obtainedClassification(:,3);
                        else
                            classes.(fileNameVar).Target = obtainedClassification(obtainedClassification(:,1)==1,4);
                        end
                    end
                    if(strcmp(dist(typeInd).long, 'Euclidean'))
                        allresults.(fileNameVar).(char(dist(typeInd).long)).('totalTime') = totalTime;
                    else
                        allresults.(fileNameVar).(char(dist(typeInd).long)).(windowName).('totalTime') = totalTime;
                    end
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
            if(window == -2)
                windowName = strcat('W_', 'Irregular');
                % Since we have 3 deterministic methods not using multiple runs
                acc_PerRun.(windowName) = zeros(numRuns, length(dist)-numDetDist);
                err_PerRun.(windowName) = zeros(numRuns, length(dist)-numDetDist);


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
                    numMeanCol = (typeInd-numDetDist)*2-1;
                    numStdCol = (typeInd-numDetDist)*2;
                    runTimesMat.(windowName)(ind, typeInd-numDetDist) = mean(allresults.(fileNameVar).(char(dist(typeInd).long)).(windowName).runTimes);
                    errIrWin.(fileNameVar)(1,[numMeanCol, numStdCol]) = [mean(err_PerRun.(windowName)(:, typeInd-numDetDist)),...
                                                                            std(err_PerRun.(windowName)(:, typeInd-numDetDist))];
                    accuracyMat.(windowName)(ind,[numMeanCol, numStdCol]) = [mean(acc_PerRun.(windowName)(:, typeInd-numDetDist)),...
                                                                             std(acc_PerRun.(windowName)(:, typeInd-numDetDist))];
                end
                minAccuracy = min( acc_PerRun.(windowName)(:) );
                maxAccuracy = max( acc_PerRun.(windowName)(:) );
            else
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
            end

            if(accMinLim>minAccuracy)
                accMinLim = floor(minAccuracy);
            end
            if(accMaxLim<maxAccuracy)
                accMaxLim = ceil(maxAccuracy);
            end
            % calculate the maximum runtime
            if(window == -2)
                for typeInd = numDetDist+1:length(dist)
                    temp = [temp, allresults.(fileNameVar).(char(dist(typeInd).long)).(windowName).runTimes'];
                end
            else
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
        
        positiveWindowSize = windowSize;
        positiveWindowSize(positiveWindowSize<0) = [];
        currFigHandle=figure('Units','inches', 'Position',[0 0 12 12]);
        set(gcf,'Visible', 'off');
        hold on
        h = zeros(1,length(dist));
        for typeInd = 1:numDetDist
            h(typeInd) = plot(positiveWindowSize, errAll.(fileNameVar)(positiveWindowSize,typeInd));
            set(h(typeInd), 'Marker', char(dist(typeInd).marker), ...
                            'MarkerSize', mrkrSz, ...
                            'LineStyle', '-', ...
                            'LineWidth', lnWd, ...
                            'Color', color_mat(typeInd,:)/255);
        end
        for typeInd = numDetDist+1:length(dist)
            h(typeInd) = plot(positiveWindowSize, errAll.(fileNameVar)(positiveWindowSize,(2*typeInd-numDetDist)-1));
            set(h(typeInd), 'Marker', char(dist(typeInd).marker), ...
                            'MarkerSize', mrkrSz, ...
                            'LineStyle', '-', ...
                            'LineWidth', lnWd, ...
                            'Color', color_mat(typeInd,:)/255);
        end
        if(sum(windowSize<0)~=0)
            for typeInd = numDetDist+1:length(dist)
                h1(typeInd) = plot(positiveWindowSize, repmat(errIrWin.(fileNameVar)(1,(typeInd-numDetDist)*2-1), 1, length(positiveWindowSize))  );
                set(h1(typeInd), 'Marker', char(dist(typeInd).marker), ...
                                'MarkerSize', mrkrSz, ...
                                'LineStyle', '-', ...
                                'LineWidth', lnWd, ...
                                'Color', color_mat(typeInd,:)/255/2);
            end
        end
        set(gca, 'XGrid', 'on', 'XMinorGrid', 'on')
        title(strcat(char(fileNames(ind)), ' - Classification Error with Increasing Window Size'), 'Interpreter', 'none', 'FontSize', fSz+2)
        xlabel('Window size', 'FontSize', fSz, 'FontName', fName);
        ylabel('Error - Lower is better', 'FontSize', fSz, 'FontName', fName);
        hLegend = legend(h, names, 'Location', 'SouthOutside', 'Orientation', 'horizontal', 'Color', 'none');
        print(gcf, format, res, char(strcat(dir.Out, '/', fileNames(ind), '/ErrorCurve', ext)));
        close gcf
        
        % start plotting the accuracy and runtime
        for window = positiveWindowSize %windowSize
            windowName = strcat('W_', num2str(window));
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
            set(gcf,'Visible', 'off');
            rtHandle = subplot(2,1,1);
            hold on
            for methodNum = 1:size(rt_Combined,2)
                bar(methodNum, mean(rt_Combined(:,methodNum), 1), 0.5, 'FaceColor', color_mat(methodNum,:)/255);
            end
            errorbar(mean(rt_Combined, 1), std(rt_Combined, 1), 'kx');
            set(rtHandle, 'XTick', 1:length(names), 'XTickLabel', names)
            accHandle = subplot(2,1,2);
            bpHandle = boxplot(acc_Combined, 'colors', color_mat/255, 'symbol', '+', 'labels', names);
%             for i = 1:size(bpHandle,2)
%                 set(bpHandle(:,i), 'LineWidth', lnWd);
%             end
            ylim([0 100])
            grid minor
            title(rtHandle, {char(strcat(char(fileNames(ind)), {' - Window size: '}, num2str(window), '%'));'Runtime'}, 'Interpreter', 'none', 'FontSize', fSz+2)
            ylabel(rtHandle, 'seconds', 'FontSize', fSz, 'FontName', fName)
            title(accHandle, 'Accuracy', 'Interpreter', 'none', 'FontSize', fSz+2)
            ylabel(accHandle, 'percentage', 'FontSize', fSz, 'FontName', fName)
            print(gcf, format, res, char(strcat(dir.Out, fileNames(ind), '/',windowName, '_Restarts', runType, ext)));
            close gcf
        end
    end

    x = -2:102; y = -2:102;
    temp = zeros(length(fileNames),3);
    xlabelString = names(1:numDetDist);
    ylabelString = names(numDetDist+1:end);
    deterministic = 1:numDetDist;
    tempsum = zeros(1,3);
    for window = positiveWindowSize %windowSize
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
                set(gcf,'Visible', 'off');
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

                print(gcf, format, res, char(strcat(dir.Out, 'Accuracy/', num2str(window), '/', char(ylabelString(distNum)), {' vs '}, char(xlabelString(det)), ext)));
                close gcf
                % Plot a ratio by ratio plot for accuracy and runtime
                figure('Units','inches', 'Position',[0 0 4 4]);
                set(gcf,'Visible', 'off');
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
                title({'Scaled Differences Accuracy and Runtime', char(strcat(ylabelString(distNum), {' and '}, xlabelString(det), {' - Window size: '}, num2str(window), '%'))}, 'FontSize', fSz+1);
                ylabel(strcat('Runtime (',char(ylabelString(distNum)),'/',char(xlabelString(det)),')'),'FontSize',fSz, 'FontName', fName);
                xlabel(strcat('Accuracy (',char(ylabelString(distNum)),'/',char(xlabelString(det)),')'),'FontSize',fSz, 'FontName', fName);
                set(gca, 'XGrid','on', 'XMinorGrid', 'off', 'YGrid','on', 'YMinorGrid', 'off','XLim',[0 2], 'YLim', [0 2]);
                plot(0:2,[1 1 1], 'Color', [0 0 0], 'LineWidth', 2);
                plot([1 1 1],0:2, 'Color', [0 0 0], 'LineWidth', 2);
                
                print(gcf, format, res, char(strcat(dir.Out, 'Ratios/', num2str(window), '/', char(ylabelString(distNum)), {' vs '}, char(xlabelString(det)), ext)));
                close gcf
            end
            distNum = distNum+1;
        end
    end
end