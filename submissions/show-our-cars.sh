egrep -A1 "19145|44907|52926|55796|59036|59708|63268|112112|68100|$(echo $(grep success submittable-cars-*.log | awk '{gsub("carid=","");gsub(",","");print $2}') | sed 's# #|#g')" cars | grep -v -- --
