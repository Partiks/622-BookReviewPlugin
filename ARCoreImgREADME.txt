Commands Reference

To use arcoreimg tool (executable) from anywhere:
echo 'alias arcoreimg="/home/partiks/622-UbuntuARAppProjects/arcore-android-sdk/tools/arcoreimg/linux/arcoreimg"' >> ~/.bashrc && exec bash

Link: https://developers.google.com/ar/develop/java/augmented-images/arcoreimg

Evaluate an image:
arcoreimg eval-img --input_image_path=who-will-cry-when-you-die.jpg

1st way (preferred): Give it a list file with specific format and it would create the AugmentedImageDatabase file
arcoreimg build-db --input_image_list_path=/home/partiks/622-UbuntuARAppProjects/parth_book_reviews_plugin/ARImages/partiks_books_img_database.imgdb-imglist.txt --output_db_path=/home/partiks/622-UbuntuARAppProjects/parth_book_reviews_plugin/ARImages/partiks_books_img_database.imgdb

2nd way: Give it a firectory, it will add every image in that directory
arcoreimg build-db --input_images_directory=/home/partiks/Documents/ar_images --output_db_path=/home/partiks/622-UbuntuARAppProjects/parth_book_reviews_plugin/ARImages/partiks_books_img_database.imgdb