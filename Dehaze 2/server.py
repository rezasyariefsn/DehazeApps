import os
import urllib.request
from flask import Flask, request, redirect, jsonify, send_from_directory
from werkzeug.utils import secure_filename

from flask import Flask

import cv2
import numpy as np
from matplotlib import pyplot as plt 
import matplotlib
matplotlib.rcParams['figure.figsize']=[10,16]

UPLOAD_FOLDER = 'C:\\Users\\Muhammad Reza SN\\Documents\\Semester 7\\PKIP dan TA\\TA\\RestAPI'

app = Flask(__name__)
app.secret_key = "secret key"
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024

ALLOWED_EXTENSIONS = set(['png', 'jpg', 'jpeg'])

def equalise_hist_color(img):
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img_yuv = cv2.cvtColor(img, cv2.COLOR_BGR2YUV)

    # equalize the histogram of the Y channel
    img_yuv[:,:,0] = cv2.equalizeHist(img_yuv[:,:,0])

    # convert the YUV image back to RGB format
    img_output = cv2.cvtColor(img_yuv, cv2.COLOR_YUV2BGR)
    return img_output

def initialize_airmap(img, beta= 0.9):
    img_c = img/255
    if img.shape[-1] >1:
        min_img = beta*(np.amin(img_c, axis = 2))
    else:
        min_img = beta*(img_c)
#     print (min_img.shape)
    return (min_img)

def refine_airmap(img):
    blur = cv2.bilateralFilter(((img)*255).astype('uint8'), 9, 75, 75)
    return blur

def restoration(img, airmap, i_inf = [1]):
    return(((img - airmap)/(1.0 - airmap/i_inf)))

def run(img):
    img = equalise_hist_color(img)

    airmap = initialize_airmap(img)
    ra = refine_airmap(airmap)/255

    b, g, r  = cv2.split(img)
    b_c = restoration(b/255, ra)
    g_c = restoration(g/255, ra)
    r_c = restoration(r/255, ra)
    img_c = cv2.merge((b_c, g_c, r_c))
    img_c = (np.clip(img_c, 0, 1)*255).astype('uint8')
    
    return img_c

def save_img(file_name, img_c):
    my_dpi=100
    
    width = img_c.shape[1]
    height = img_c.shape[0]

    fig = plt.figure(figsize=(width/my_dpi, height/my_dpi), dpi=my_dpi)

    ax = plt.Axes(fig, [0., 0., 1., 1.])
    ax.set_axis_off()
    fig.add_axes(ax)

    ax.imshow(img_c, aspect='auto')
    fig.savefig(file_name)

    return file_name

@app.route('/')
def index():
  return 'Server Works!'

def allowed_file(filename):
	return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'],
                               filename)

@app.route('/upload', methods=['POST'])
def upload_file():
  # check if the post request has the file part
  if 'file' not in request.files:
    resp = jsonify({'message' : 'No file part in the request'})
    resp.status_code = 400
    return resp
  file = request.files['file']
  if file.filename == '':
    resp = jsonify({'message' : 'No file selected for uploading'})
    resp.status_code = 400
    return resp
  if file and allowed_file(file.filename):
    filename = secure_filename(file.filename)
    file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
    
    image_ori = cv2.imread(filename)
    img_c = run(image_ori)
    file_res = save_img('hasil_{}'.format(filename), img_c)

    resp = jsonify({'file_url' : 'http://192.168.1.3:5000/uploads/{}'.format(file_res)})
    resp.status_code = 200
    return resp
  else:
    resp = jsonify({'message' : 'Allowed file types are png, jpg, jpeg'})
    resp.status_code = 400
    return resp

if __name__ == "__main__":
    app.run(host="192.168.1.3", port=5000)