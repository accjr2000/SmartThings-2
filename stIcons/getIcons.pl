#!/usr/bin/perl

$BASE = 'https://s3.amazonaws.com/smartapp-icons/';

`curl -s $BASE -o smart-app-icons.xml`;

open IN, "smart-app-icons.xml";
open OUT, ">iconUrls.txt";

while ($line = <IN>) {
   while ($line =~ /<Key>(.*?)<\/Key>/g){
      $url = "$BASE$1";
      if ($url =~ /\.png$/) {

         print OUT "$url\n";

         print "Getting icon: $url\n";
         #`curl -O -s $url`;
      }
   }
}
close IN;
close OUT;


