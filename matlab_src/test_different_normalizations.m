inst = -5 +(5+5)*rand(1, 10)
x=1:length(inst)
inst_simple = -1+2*(inst-min(inst))./(max(inst)-min(inst))
inst_z = (inst-mean(inst))./std(inst)
inst_logistic = -1+2./(1+exp(-1*inst))
inst_tanh = tanh(inst)
inst_erf = erf(inst)

plot(  x,inst,  x,inst_simple, x,inst_z, x,inst_logistic, x,inst_tanh, x,inst_erf)
grid on
legend('inst', 'inst simple',  'inst z', 'inst logistic', 'inst tanh', 'inst erf')