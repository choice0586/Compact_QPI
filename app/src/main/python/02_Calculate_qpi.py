import numpy as np
import os
import math
import matplotlib.pyplot as plt
from PIL import Image
import cv2 as cv
from qpi_functions import ph_tick

# 안드로이드로 새로 추가한거
from com.chaquo.python import Python
import io
import base64

def main():
    # 핸드폰 디렉토리에서 찍은 4장 가지고 오기
    files_dir = str(Python.getPlatform().getApplication().getFilesDir())

    imglist = []
    for fname in ["0001.png", "0002.png", "0003.png", "0004.png"]:
        imglist.append(os.path.join(os.path.dirname(files_dir), fname))       

    qpi_path = os.path.join(files_dir, "results")  
     
     # 계산에 쓸 여러 값들
    rot_ang_list = (0, -0.5*math.pi)
    n_an = len(rot_ang_list)
    reg = 10.0**(-0.9) # 이게 알파값    

    # 데이터 공간 만들기
    Np = (720, 1080) # 해상도 조정하기 ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
    img_i = np.zeros((Np[0],  Np[1]  ,4))
    img_dpc = np.zeros((Np[0],  Np[1],  n_an))
    Hi = np.zeros((Np[0],  Np[1],  n_an))

    # transfer function 불러오기
    Hitb_path = os.path.join(os.path.dirname(__file__), "Hitb.png") # tiff 파일은 못읽어와서 png로 바꿈
    Hilr_path = os.path.join(os.path.dirname(__file__), "Hilr.png")
    Hi[:,:,0] = cv.imread(Hitb_path, -1)
    Hi[:,:,1] = cv.imread(Hilr_path, -1)
    
    Hi = (Hi/(2**8-1)*2-1).astype(np.float32) # [0 255] ------> [-1 1] - 계산 해서 넣어주기
    Hph = np.zeros(Hi.shape, dtype=np.complex)
    Hph.imag = Hi

    for ii in range(np.int(len(imglist)/4)):
        
        for iii in range(2):

            temp = cv.imread(imglist[ii*4+iii*2], cv.IMREAD_GRAYSCALE)
            temp = np.asarray(temp,dtype=np.float32)
            temp1 = cv.imread(imglist[ii*4+iii*2+1], cv.IMREAD_GRAYSCALE)
            temp1 = np.asarray(temp1,dtype=np.float32)

            img_dpc[:,:,iii] = np.asarray(np.divide(temp/2 - temp1/2 + np.spacing(1), temp/2+temp1/2 + np.spacing(1) ),dtype=np.float32)
            # np.spacing(1)은 엄청 작은값인데 0으로 나눠주는거 방지!

        ph_dpc = ph_tick(img_dpc, Hph, reg)/ Np[0]/ Np[1]   # 여기서 ph_thick 나옴

        qpi_name = f'{ii:04}.png'
        dpc1_name = f'{ii:04}dpcTB.png'
        dpc2_name = f'{ii:04}dpcLR.png'

        # 다시 [0 255] 값으로 만들기 위해    
        ph_min = np.min(ph_dpc)
        ph_max = np.max(ph_dpc)

        # Save

        # -- QPI
        ph_8bit = np.array((ph_dpc-ph_min)/(ph_max-ph_min)*255 ,dtype=np.uint8)
        qpi_name_path = os.path.join(os.path.dirname(files_dir), qpi_name)        
        cv.imwrite(qpi_name_path, ph_8bit)

        # -- QPI는 안드로이드로 return 해서 화면에 띄우기
        qpi_8bit = Image.fromarray(ph_8bit)        
        buff = io.BytesIO()
        qpi_8bit.save(buff, format="PNG")
        img_str = base64.b64encode(buff.getvalue())              
            
        
        # -- dpc_tb
        dpctb_8bit = np.array(127*img_dpc[:,:,0]+128,dtype=np.uint8)
        dpc1_name_path = os.path.join(os.path.dirname(files_dir), dpc1_name)
        cv.imwrite(dpc1_name_path, dpctb_8bit)             

        # -- dpc_lr
        dpclr_8bit = np.array(127*img_dpc[:,:,1]+128,dtype=np.uint8)
        dpc2_name_path = os.path.join(os.path.dirname(files_dir), dpc2_name)
        cv.imwrite(dpc2_name_path, dpclr_8bit)    

    return "" + str(img_str, 'utf-8')    
