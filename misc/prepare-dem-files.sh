##################
# Montpellier DSM
##################

#775850 775310 774770 774230 773690 773150 772610 772070 771530 770990 770450 769910 769370 768830 768290 767750 767210 766670 766130 765590 765050 
#765050 765590 766130 766670 767210 767750 768290 768830 769370 769910 770450 770990 771530  772070 772610 773150 773690 774230 774770 775310 775850

#ALL-DSM-765050.tif ALL-DSM-765590.tif ALL-DSM-766130.tif ALL-DSM-766670.tif ALL-DSM-767210.tif ALL-DSM-767750.tif ALL-DSM-768290.tif ALL-DSM-768830.tif ALL-DSM-769370.tif ALL-DSM-769910.tif ALL-DSM-770450.tif ALL-DSM-770990.tif ALL-DSM-771530.tif ALL-DSM-772070.tif ALL-DSM-772610.tif ALL-DSM-773150.tif ALL-DSM-773690.tif ALL-DSM-774230.tif ALL-DSM-774770.tif ALL-DSM-775310.tif ALL-DSM-775850.tif
ALL-DSM-775850.tif ALL-DSM-775310.tif ALL-DSM-774770.tif ALL-DSM-774230.tif ALL-DSM-773690.tif ALL-DSM-773150.tif ALL-DSM-772610.tif ALL-DSM-772070.tif ALL-DSM-771530.tif ALL-DSM-770990.tif ALL-DSM-770450.tif ALL-DSM-769910.tif ALL-DSM-769370.tif ALL-DSM-768830.tif ALL-DSM-768290.tif ALL-DSM-767750.tif ALL-DSM-767210.tif ALL-DSM-766670.tif ALL-DSM-766130.tif ALL-DSM-765590.tif ALL-DSM-765050.tif

#declare -a files=(765050 765590 766130 766670 767210 767750 768290 768830 769370 769910 770450 770990 771530  772070 772610 773150 773690 774230 774770 775310 775850); for i in "${files[@]}"; do gdal_merge.py ALL-DSM-$i*.tif -o ALL-DSM.tif; done

#gdal_merge.py ALL-DSM-765050.tif ALL-DSM-765590.tif ALL-DSM-766130.tif ALL-DSM-766670.tif ALL-DSM-767210.tif ALL-DSM-767750.tif ALL-DSM-768290.tif ALL-DSM-768830.tif ALL-DSM-769910.tif ALL-DSM-770450.tif ALL-DSM-770990.tif ALL-DSM-771530.tif ALL-DSM-772070.tif ALL-DSM-772610.tif ALL-DSM-773150.tif ALL-DSM-773690.tif ALL-DSM-774230.tif ALL-DSM-774770.tif ALL-DSM-775310.tif ALL-DSM-775850.tif -o ALL-DSM.tif
gdal_merge.py ALL-DSM-775850.tif ALL-DSM-775310.tif ALL-DSM-774770.tif ALL-DSM-774230.tif ALL-DSM-773690.tif ALL-DSM-773150.tif ALL-DSM-772610.tif ALL-DSM-772070.tif ALL-DSM-771530.tif ALL-DSM-770990.tif ALL-DSM-770450.tif ALL-DSM-769910.tif ALL-DSM-769370.tif ALL-DSM-768830.tif ALL-DSM-768290.tif ALL-DSM-767750.tif ALL-DSM-767210.tif ALL-DSM-766670.tif ALL-DSM-766130.tif ALL-DSM-765590.tif ALL-DSM-765050.tif -o ALL-DSM.tif

gdal_polygonize.py 773690_6281500_50cm_dsm.tif gluar.shp

##################
# Montpellier DTM
##################

ogr2ogr -skipfailures ALL-DTM.shp Centre_MNT_2016.dxf
ogr2ogr -update -append -skipfailures ALL-DTM.shp Cevennes_MNT_2016.dxf -nln merge
ogr2ogr -update -append -skipfailures ALL-DTM.shp Cevennes_MNT_2016.dxf
ogr2ogr -update -append -skipfailures ALL-DTM.shp Croix_d_Argent_MNT_2016.dxf
ogr2ogr -update -append -skipfailures ALL-DTM.shp Hopitaux_Facultés_MNT_2016.dxf
ogr2ogr -update -append -skipfailures ALL-DTM.shp Mosson_MNT_2016.dxf
ogr2ogr -update -append -skipfailures ALL-DTM.shp Port_Marianne_MNT_2016.dxf
ogr2ogr -update -append -skipfailures ALL-DTM.shp Prés_d_Arènes_MNT_2016.dxf

gdal_grid ALL-DTM.shp ALL-DTM.tiff -a_srs EPSG:2154 -outsize 1024 1024 -a nearest:nodata=-9999

ogr2ogr.exe -where nom="'Montpellier'" CommuneMontpellier.shp /e/Geodata/Local/Countries/France/AdminAreas/OSM/communes-20170112.shp
gdalwarp -cutline CommuneMontpellier.shp ALL-DTM-full.tiff ALL-DTM.tiff
