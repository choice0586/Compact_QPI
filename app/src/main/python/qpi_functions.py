import numpy as np
import os
import math
import cv2 as cv

#######################
# FFT and inverse FFT #
#######################
def ft (object):
    obj_ish = np.fft.ifftshift(object)
    obj_ish_ft = np.fft.fft2(obj_ish)
    obj_ish_ft_sh = np.fft.fftshift(obj_ish_ft)
    return obj_ish_ft_sh

def ift (objectp):
    objp_ish = np.fft.ifftshift(objectp)
    objp_ish_ift = np.fft.fft2(objp_ish)
    objp_ish_ift_sh = np.fft.fftshift(objp_ish_ift)
    return objp_ish_ift_sh

###################
# Source function #
###################
def Dsource_LR (rot_angle, NAp, lambdaa, uuu, vvv):
    sss = lambdaa*np.sqrt(np.square(uuu)+np.square(vvv))
    s0=np.where(sss <= (NAp+0.1), 1, 0)
    
    LR = np.zeros(uuu.shape)
    
    LR [vvv > uuu*math.tan(rot_angle)] = 1
    LR [vvv <= uuu*math.tan(rot_angle)] = -1
    
    return np.multiply(s0,LR)

#####################
# Transfer function #
#####################
def Transf (pupilp, sf):
    sfp_ft =ft(np.multiply(sf,pupilp))
    p_ft = ft(pupilp)
    
    abH = -ift(np.multiply(np.conj(sfp_ft),p_ft))-np.conj(ift(np.multiply(sfp_ft,np.conj(p_ft))))
    phH = ift(np.multiply(np.conj(sfp_ft),p_ft))-np.conj(ift(np.multiply(sfp_ft,np.conj(p_ft))))
    
    magH = np.sqrt(np.square(np.abs(abH))+np.square(np.abs(phH)))
    
    abHp = np.divide(abH, magH.max())
    phHp = np.divide(phH, magH.max())

    return abHp, phHp

########################
# Phase reconstruction #
########################
def ph_tick (idpc, hh, regp):
    numer = np.zeros(idpc.shape, dtype=np.complex128)
    for i in range(2):
        numer[:,:,i] = np.multiply(ft(idpc[:,:,i]),np.conj(hh[:,:,i]))

    denom = np.square(np.abs(hh))
    
    ph = -np.real(ift(np.divide(numer.sum(2), denom.sum(2)+regp)))
    
    return ph