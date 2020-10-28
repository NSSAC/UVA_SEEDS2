#The R code uses the model trained from the synthetic data.

#Specify the path of directory to your file
path.dir<-"/Users/huzhihao/Documents/STAT/NDSSL/DOE_SEEDSII/Decision adjusted for synthetic data/tools/"
#Read the coefficients
beta<-read.csv(paste0(path.dir,"beta_coef-v1.csv"),header=TRUE)
#Read your input file/variable values of households.
x_input<-read.csv(paste0(path.dir,"sample_input-v1.csv"),header=TRUE) #The user should change the input file name to his own.
#x_input<-read.csv(paste0(path.dir,"input-v1.csv"),header=TRUE)
#Create the indicator variable for totalVal.
n<-dim(x_input)[1]
x.totalVal<-rep(0,n)
x.totalVal[which(x_input$totalVal>75000)]<-1
x_input<-cbind(x_input,x.totalVal)
x_input<-data.matrix(x_input)

#Compute the probability to be solar panel adopters
prob<-exp(x_input%*%beta$Coefficient)/(1+exp(x_input%*%beta$Coefficient))

#Create the label for households. 1 represents solar panel adopters, 0 represents non-adopters
label<-rep(0,n)
label[which(prob>=0.0022)]<-1

#Export results probablity and label to csv files in the same directory where the R file locates in.
household<-1:n;probability<-prob
prob_frame<-data.frame(household,prob)
label_frame<-data.frame(household,label)
write.csv(prob_frame,file=paste0(path.dir,"static-probability-of-adoption-v1.csv"),row.names=F) #probability
write.csv(label_frame,file=paste0(path.dir,"predicted-label-v1.csv"),row.names=F) #label


