#!/bin/bash

#rodar o total de clientes SOA que serão utilizados.
cd /storage1/dados/es91661/ExecAIGA

total=2
folder="/storage1/dados/es91661/ExecAIGA/configSOA/SOA"

for ((m=0; m<total;m++)) ;  
do
	folder="/storage1/dados/es91661/ExecAIGA/configSOA/SOA"$m
	(qsub -l nodes=1:ppn=5,mem=5gb -v FOLDER=$folder UpClientSOA.sh) &
done


