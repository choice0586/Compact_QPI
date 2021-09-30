import base64
import numpy as np
import cv2 as cv
from com.chaquo.python import Python
import os

def main(bitmap_string_image, i):
    # load img
    decoded_data = base64.b64decode(bitmap_string_image) # decode
    np_data = np.fromstring(decoded_data, np.uint8) # change to np
    img = cv.imdecode(np_data, cv.IMREAD_UNCHANGED) # load to cv
    img_gray = cv.cvtColor(img, cv.COLOR_BGR2GRAY) # change to gray  
    
    # save
    files_dir = str(Python.getPlatform().getApplication().getFilesDir())  
    name = f'{i:04}.png'
    save_path = os.path.join(os.path.dirname(files_dir), name)
    cv.imwrite(save_path, img_gray)            

    return "saved as " + name

