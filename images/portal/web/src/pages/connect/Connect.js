import React, { useEffect, useRef, useState } from 'react';
import Guacamole from 'guacamole-common-js';
import Copy from 'copy-to-clipboard';
import { saveAs } from 'file-saver';
import { message } from 'antd';
import { useTranslation } from 'react-i18next';
import FilePanel from '../../components/fileManagement/FilePanel';
import './Connect.css';
import FileManagerExpandIcon from '../../assets/image/file-expand.png';
import FileManagerCollapseIcon from '../../assets/image/file-collapse.png';
import { 
  getConnectInfo, 
  checkConnect,
  aliveConnect,
} from '../../api/api.js';
import TextInput from './textInput/TextInput';

/**
     * All client error codes handled and passed off for translation. Any error
     * code not present in this list will be represented by the "DEFAULT"
     * translation.
     */
 let CLIENT_ERRORS = {
  0x0201: true,
  0x0202: true,
  0x0203: true,
  0x0207: true,
  0x0208: true,
  0x0209: true,
  0x020A: true,
  0x020B: true,
  0x0301: true,
  0x0303: true,
  0x0308: true,
  0x031D: true
};

/**
* All tunnel error codes handled and passed off for translation. Any error
* code not present in this list will be represented by the "DEFAULT"
* translation.
*/
let TUNNEL_ERRORS = {
  0x0201: true,
  0x0202: true,
  0x0203: true,
  0x0204: true,
  0x0205: true,
  0x0207: true,
  0x0208: true,
  0x0301: true,
  0x0303: true,
  0x0308: true,
  0x031D: true
};

const isTouchDevice = () => {
  return 'ontouchstart' in document.documentElement;
};

function isControlCharacter(codepoint) {
  return codepoint <= 0x1F || (codepoint >= 0x7F && codepoint <= 0x9F);
}
function isUnicode(codepoint) {
  // Keysyms for Unicode
  if (codepoint >= 0x00FFFFFF && codepoint <= 0x01FFFFFF) {
    return true;
  }
  return false;
}

const ctrl_keysym = 65507;
const tab_keysym = 65289;
const alt_keysym = 65513;
const esc_keysym = 65307;
const shift_keysym = 65505;

let initialHeight = window.innerHeight;
const inputTextAreaHeight = 36;

const Connect = (props) => {
  const { t } = useTranslation();
  const containerRef = useRef();
  const displayRootRef = useRef();
  const displayRef = useRef();
  const filePanelCollapseTransform = 'matrix(1, 0, 0, 1, 600, 0)';
  const filePanelExpandedTransformWithShareFileCollapse = 'matrix(1, 0, 0, 1, 300, 0)';
  const fileButtonExpandedTransform = 'matrix(1, 0, 0, 1, -300, -27.2222)';
  const fileButtonCollapseTransform = 'matrix(1, 0, 0, 1, 0, -27.2222)';
  const [filePanelVisible, setfilePanelVisible] = useState(false);
  const fielPanelRef = useRef();
  const guacClient = useRef();
  const guacKeyboard = useRef();
  const guacMouse = useRef();
  const guacTouch = useRef();
  const guacInputSink = useRef();
  const fileSystem = useRef();
  const localConnectInfo = useRef();
  const displayScale = useRef();
  const [remoteFile, setRemoteFile] = useState(null);
  const [connectInfo, setConnectInfo] = useState({});

  const guacLink = {};

  const [connectState, setConnectState] = useState(0);
  const [tunnelState, setTunnelState] = useState(0);
  const [errorMessage, setErrorMessage] = useState(null);
  const [connectCheckFinish, setConnectCheckFinish] = useState(false);

  // SSH 上传状态
  const [isSSHUploading, setIsSSHUploading] = useState(false);
  const [isSSHUploadSuccess, setIsSSHUploadSuccess] = useState(true);

  let textInputFocus = false;

  // Text Input
  const [ctrlKeyPressed, setCtrlKeyPressed] = useState(false);
  const [altKeyPressed, setAltlKeyPressed] = useState(false);
  const [shiftKeyPressed, setShiftKeyPressed] = useState(false);
  const [escKeyPressed, setEscKeyPressed] = useState(false);
  const [tabKeyPressed, setTabKeyPressed] = useState(false);
  const [curText, setCurText] = useState('');
  const onTextInputFocus = (focus) => {
    textInputFocus = focus;
    if (!focus) {
      // On blur, restore key pressed.
      if (ctrlKeyPressed) {
        sendKeyEvent(0, ctrl_keysym);
        setCtrlKeyPressed(false);
      }
      if (altKeyPressed) {
        sendKeyEvent(0, alt_keysym);
        setAltlKeyPressed(false);
      }
      if (shiftKeyPressed) {
        setShiftKeyPressed(false);
      }
      if (tabKeyPressed) {
        setTabKeyPressed(false);
      }
    }
  }

  const clearText = () => {
    setCurText('');
    setTimeout(() => {
      clearText();
    }, 1000);
  }

  //  * Whether composition is currently active within the text input
  //  * target element, such as when an IME is in use.
  //  *
  //  * @type Boolean
  //  */
  let composingText = false;
  const ua = navigator.userAgent.toLowerCase();
  const isSafari = ua.indexOf('chrome') === -1 && ua.indexOf('safari') !== -1;

  const sendKeyEvent = (type, code) => {
    if (composingText && !isSafari) {
      return;
    }
    if (localConnectInfo.current.keyboard && guacClient.current) {
      // console.log('sendKeyEvent', type, code);
      if (localConnectInfo.current && localConnectInfo.current.input !== null && localConnectInfo.current.input === 0 && isUnicode(code)) {
        // console.log('send keyevent unicode', type, code);
      } else {
        guacClient.current.sendKeyEvent(type, code);
      }
    }
  }

  const sendMouseState = (state) => {
    if (guacClient.current && (state.left || state.right)) {
      if (!guacKeyboard.current.onkeyup) {
        const input = document.getElementById('clipboard-input');
        if (input) {
          input.blur();
        }
        changeFilePanelVisible(false);
      }
    }
    if (localConnectInfo.current.mouse && guacClient.current) {
      guacClient.current.sendMouseState(state);
    }
  }

  const funcOnkeydown = (keysym) => {
    sendKeyEvent(1, keysym);
  }
  const funcOnkeyup = (keysym) => {
    sendKeyEvent(0, keysym);
  }

  const textInputOnkeydown = (keyEvent) => {
    if (isTouchDevice() && textInputFocus) {
      return;
    }
  }
  const textInputOnkeyup = (keyEvent) => {
    if (isTouchDevice() && textInputFocus) {
      if (keyEvent.keyCode === 8) {
        const textValue = curText.substr(0, curText.length - 1);  
        setCurText(textValue);
        return;
      }

      if (ctrlKeyPressed) {
        sendKeyEvent(0, ctrl_keysym);
        setCtrlKeyPressed(false);
      }
      if (shiftKeyPressed) {
        sendKeyEvent(0, shift_keysym);
        setShiftKeyPressed(false);
      }

      if (isControlCharacter(keyEvent.keyCode)) {
        return;
      }
      const textValue = curText + keyEvent.key;
      setCurText(textValue);
      return;
    }
  }

  const onTextChange = (text) => setCurText(text)

  const onButtonPressed = (key) => {
    switch (key) {
      case 'Ctrl':
        if (!ctrlKeyPressed) {
          sendKeyEvent(1, ctrl_keysym);
        } else {
          sendKeyEvent(0, ctrl_keysym);
        }
        setCtrlKeyPressed(!ctrlKeyPressed);
        break;
      case 'Alt':
        if (!altKeyPressed) {
          sendKeyEvent(1, alt_keysym);
        } else {
          sendKeyEvent(0, alt_keysym);
        }
        setAltlKeyPressed(!altKeyPressed);
        break;
      case 'Shift':
        if (!shiftKeyPressed) {
          sendKeyEvent(1, shift_keysym);
        } else {
          sendKeyEvent(0, shift_keysym);
        }
        setShiftKeyPressed(!shiftKeyPressed);
        break;
      case 'Esc':
        setEscKeyPressed(true);
        sendKeyEvent(1, escKeyPressed);
        sendKeyEvent(0, escKeyPressed);
        setEscKeyPressed(false);
        if (ctrlKeyPressed) {
          sendKeyEvent(0, ctrl_keysym);
          setCtrlKeyPressed(false);
        }
        if (altKeyPressed) {
          sendKeyEvent(0, alt_keysym);
          setAltlKeyPressed(false);
        }
        break;
      case 'Tab':
        setTabKeyPressed(true);
        sendKeyEvent(1, tabKeyPressed);
        sendKeyEvent(0, tabKeyPressed);
        setTabKeyPressed(false);
        if (ctrlKeyPressed) {
          sendKeyEvent(0, ctrl_keysym);
          setCtrlKeyPressed(false);
        }
        if (altKeyPressed) {
          sendKeyEvent(0, alt_keysym);
          setAltlKeyPressed(false);
        }
        break;
      default: 
        break;
    }
  }

  const changeFilePanelVisible = (visible) => {
    let fileManagerContainer = document.getElementById('file-panel');
    if (fileManagerContainer) {
      let transform = !visible ? filePanelCollapseTransform : filePanelExpandedTransformWithShareFileCollapse;
      fileManagerContainer.style.transform = transform;
    }
    setfilePanelVisible(visible);
    if (guacKeyboard.current) {
      if (visible) {
        guacKeyboard.current.onkeydown = null;
        guacKeyboard.current.onkeyup = null;
      } else {
        guacKeyboard.current.onkeydown = funcOnkeydown;
        guacKeyboard.current.onkeyup = funcOnkeyup;
      }
    }
  };

  const setClipboard = (data) => {
    let stream = guacClient.current.createClipboardStream('text/plain');

    let writer = new Guacamole.StringWriter(stream);
    for (let i = 0; i < data.length; i += 4096) {
      writer.sendText(data.substring(i, i + 4096));
    }

    writer.sendEnd();
  };

  function getDisplayAreaHeight() {
    if (isTouchDevice() && !filePanelVisible) {
      return window.innerHeight - inputTextAreaHeight;
    }
    return window.innerHeight;
  }

  function adjustDisplayArea() {
    // Display Node
    const displayAreaHeight = getDisplayAreaHeight();
    let displayNode = document.getElementById('display');
    displayNode.style.height = displayAreaHeight + 'px';
    displayNode.style.width = window.innerWidth + 'px';

    // Container Node
    let containerNode = document.getElementById('client-container');
    containerNode.style.height = window.innerHeight + 'px';
    containerNode.style.width = window.innerWidth + 'px';

    return displayAreaHeight;
  }

  /**
       * Resizes the container element inside the guacViewport such that
       * it exactly fits within the visible area, even if the browser has
       * been scrolled.
       */
  function fitVisibleArea() {
    if (!isTouchDevice() || !guacClient.current || !displayScale.current) {
      return;
    }

    let density = window.devicePixelRatio || 1;
    // Calculate viewport dimensions (this is NOT necessarily the
    // same as 100vw and 100vh, 100%, etc., particularly when the
    // on-screen keyboard of a mobile device pops open)
    const displayAreaHeight = adjustDisplayArea();
    let displayNode = document.getElementById('display');
    displayNode.style.bottom = inputTextAreaHeight + 'px';
    displayNode.style.position = 'fixed';

    const verticalScale = displayAreaHeight / initialHeight;
    guacClient.current.getDisplay().scale(verticalScale / displayScale.current);
    const displayXOffset = (1 - verticalScale) * window.innerWidth / density;
    displayNode.style.left = displayXOffset + 'px';
  };

  let retryCount = 0;

  function connectToGuacclient(url) {
    setErrorMessage(null);

    let container = containerRef.current;
    let display = displayRef.current;
    if (container && display) {
      if (display.lastChild) {
        display.removeChild(display.lastChild);
      }
      if (guacKeyboard.current) {
        guacKeyboard.current.onkeydown = null;
        guacKeyboard.current.onkeyup = null;
      }
      if (guacMouse.current) {
        guacMouse.current.onmousedown = guacMouse.current.onmouseup = guacMouse.current.onmousemove = null;
      }
      if (guacTouch.current) {
        guacTouch.current.onmousedown = guacTouch.current.onmouseup = guacTouch.current.onmousemove = null;
      }

      const displayAreaHeight = adjustDisplayArea();
      initialHeight = displayAreaHeight;
      let density = window.devicePixelRatio || 1;
      let width = window.innerWidth;
      let height = displayAreaHeight;
      let tunnel = new Guacamole.WebSocketTunnel(
          '/api/tunnel?id=' + url + 
          '&WIDTH=' + Math.floor(width * density) +
          '&HEIGHT=' + Math.floor(height * density) + 
          '&DPI=' + Math.floor(96 * density)
      );
      let guac = new Guacamole.Client(tunnel);
      guacLink.link = guac;
      guacClient.current = guac;
      let scale = 1;

      guac.onfilesystem = function(object) {
        console.log('connect filesystem', object);
        if (localConnectInfo.current.protocol === 'SSH') {
          fileSystem.current = object;
          
          //监听onbody事件，对返回值进行处理，返回内容可能有两种，一种是文件夹，一种是文件。
          object.onbody = function(stream, mimetype, filename) {
            stream.sendAck('OK', Guacamole.Status.Code.SUCCESS);
            downloadFile(stream, mimetype, filename);
          }

          refreshDirectory('/');
        }
      }

      // Add client to display div
      display.appendChild(guac.getDisplay().getElement());

      // Error handler
      guac.onerror = function(error) {
        // console.log('guac error', error);

        // Determine translation name of error
        const status = error.code;
        let errorName = (status in CLIENT_ERRORS) ? status.toString(16).toUpperCase() : 'DEFAULT';
        let errorKey = 'ERROR_CLIENT_' + errorName;
        let errorMsg = t(errorKey);

        if (retryCount > 0) {
          // alert(errorMsg);
          setErrorMessage(errorMsg);
        }
      };
      // Connect
      guac.connect();

      // Mouse
      let mouse = new Guacamole.Mouse(guac.getDisplay().getElement());

      mouse.onmousedown = mouse.onmouseup = mouse.onmousemove = function(mouseState) {
        let scaledState = new Guacamole.Mouse.State(
          mouseState.x * scale,
          mouseState.y * scale,
          mouseState.left,
          mouseState.middle,
          mouseState.right,
          mouseState.up,
          mouseState.down
        );
        sendMouseState(scaledState);
      };
      guacMouse.current = mouse;

      // Touch
      let touch = new Guacamole.Mouse.Touchscreen(guac.getDisplay().getElement()); // or Guacamole.Touchscreen
      touch.onmousedown = touch.onmousemove = touch.onmouseup = function(mouseState) {
        let scaledState = new Guacamole.Mouse.State(
          mouseState.x * scale,
          mouseState.y * scale,
          mouseState.left,
          mouseState.middle,
          mouseState.right,
          mouseState.up,
          mouseState.down
        );
        sendMouseState(scaledState);
      };
      guacTouch.current = touch;

      // Keyboard
      guacKeyboard.current = new Guacamole.Keyboard(document);
      guacKeyboard.current.onkeydown = funcOnkeydown;
      guacKeyboard.current.onkeyup = funcOnkeyup;

      if (guacInputSink.current) {
        guacKeyboard.current.listenTo(guacInputSink.current.getElement());
      }

      if (isTouchDevice()) {
        setTimeout(() => {
          clearText();
        }, 1000);
      }

      // Mobile
      window.keyEventCallback = (down, keysym) => {
        sendKeyEvent(down, keysym)
      }

      window.mouseEventCallback = (x, y, left, middle, right, up, down) => {
        let scaledState = new Guacamole.Mouse.State(
          x * scale,
          y * scale,
          left,
          middle,
          right,
          up,
          down
        );
        sendMouseState(scaledState);
      }

      tunnel.onstatechange = function(state) {
        setTunnelState(state);
      }

      tunnel.onerror = function(error) {
         // Determine translation name of error
        const status = error.code;
        let errorName = (status in CLIENT_ERRORS) ? status.toString(16).toUpperCase() : 'DEFAULT';
        let errorKey = 'ERROR_TUNNEL_' + errorName;
        let errorMsg = t(errorKey);
        
        if (retryCount > 0) {
          // alert(errorMsg);
          setErrorMessage(errorMsg);
        }
      };

      let updateScale = function() {
        const displayAreaHeight = adjustDisplayArea();
        let width = window.innerWidth;
        let height = displayAreaHeight;
        let canvases = document.getElementsByTagName('canvas');
        let cWidth = 0;
        let cHeight = 0;

        if (canvases && canvases.length) {
          let canvas = canvases[0].parentElement;
          cWidth = canvas.style.width
          cHeight = canvas.style.height
          cWidth = parseFloat(cWidth.substring(0, cWidth.length - 2))
          cHeight = parseFloat(cHeight.substring(0, cHeight.length - 2))

          if (cWidth > 0 && cHeight > 0) {
            scale = cWidth / width;
            if (scale < cHeight / height) {
              scale = cHeight / height;
            }
          }
          scale = scale.toFixed(3);
          displayScale.current = scale;
        }

        if (Math.abs((1 / scale) - guac.getDisplay().getScale()) > 0.001) {
          const display = guac.getDisplay();
          display.scale(1 / scale);
          // guac.getDisplay().resize(guac.getDisplay().getDefaultLayer(), 
          //   width * scale, height * scale);
        }
      }

      guac.onstatechange = function(state) {
        if (state === 5 && retryCount < 1) {
          setConnectState(1)
          setTimeout(() => {
            retryCount = retryCount + 1;
            connectToGuacclient(url);
          }, 10000);
        } else {
          setConnectState(state);
        }
        if (state === 3) {
          updateScale();
          requestAudioStream(guac);

          setTimeout(() => {
            const displayAreaHeight = adjustDisplayArea();
            let width = window.innerWidth;
            let height = displayAreaHeight;
            // guac.sendSize((width).toFixed(0), (height).toFixed(0))
            let density = window.devicePixelRatio || 1;
            guac.sendSize(
              (width * density).toFixed(0), (height * density).toFixed(0));
          }, 1000)
          
          let canvases = document.getElementsByTagName('canvas');
          if (canvases && canvases.length) {
            const observer = new MutationObserver((aa) => {
              updateScale()
            })
            observer.observe(canvases[0], {
              attributes: true
            })
          }
        }
      };

      guac.onclipboard = function(stream, mimetype) {
        if (/^text\//.exec(mimetype)) {
          let stringReader = new Guacamole.StringReader(stream);
          let content = '';
          stringReader.ontext = function ontext(text) {
            content += text.replace('\u0000', '');
          };
          stringReader.onend = function() {
            if (!localConnectInfo.current.copy && content.length > 0) {
              message.warning('Copy Operation is disabled.');
              return;
            }
            if (content.length <= 0) {
              return;
            }
            let clipboardElement = document.getElementById('clipboard-input');
            if (clipboardElement) {
              clipboardElement.value = content;
            }
            Copy(content);
          };
        }
      };

      const downloadFile = (stream, mimetype, filename) => {
        let blob_builder;
        if (window.BlobBuilder) {
          blob_builder = new Guacamole.BlobBuilder();
        } else if (window.WebKitBlobBuilder) {
          blob_builder = new Guacamole.WebKitBlobBuilder();
        } else if (window.MozBlobBuilder) {
          blob_builder = new Guacamole.MozBlobBuilder();
        } else {
          blob_builder = new function() {
            let blobs = [];
            /** @ignore */
            this.append = function(data) {
              blobs.push(new Blob([data], { 'type': mimetype }));
            };

            /** @ignore */
            this.getBlob = function() {
              return new Blob(blobs, { 'type': mimetype });
            };
          }();
        }

        stream.onblob = function(data) {
          // Convert to ArrayBuffer
          let binary = window.atob(data);
          let arrayBuffer = new ArrayBuffer(binary.length);
          let bufferView = new Uint8Array(arrayBuffer);
  
          for (let i = 0; i < binary.length; i++) {
            bufferView[i] = binary.charCodeAt(i);
          }
  
          blob_builder.append(arrayBuffer);
          // length += arrayBuffer.byteLength;
          // Send success response
          stream.sendAck('OK', 0x0000);
        };
  
        // 结束后的操作
        stream.onend = function() {
          //获取整合后的数据
          let blob_data = blob_builder.getBlob();
  
          //数据传输完成后进行下载等处理
          if (mimetype.indexOf('stream-index+json') !== -1) {
            //如果是文件夹,需要解决如何将数据读出来，这里使用filereader读取blob数据，最后得到一个json格式数据
            let blob_reader = new FileReader();
            blob_reader.addEventListener('loadend', function() {
              let folder_content = JSON.parse(blob_reader.result)
              //这里加入自己代码，实现文件目录的ui，重新组织当前文件目录
              let remoteFile = { path: filename, files: [] };
              Object.keys(folder_content).forEach(key => {
                remoteFile.files.push({
                  path: key,
                  name: key.substring(key.lastIndexOf('/') + 1),
                  isDirectory: folder_content[key] === Guacamole.Object.STREAM_INDEX_MIMETYPE
                })
              });
              setRemoteFile(remoteFile);
            });
            blob_reader.readAsText(blob_data, 'UTF-8');
          } else {
            let file_arr = filename.split('/');
            let download_file_name = file_arr[file_arr.length - 1];
            saveAs(blob_data, download_file_name);
          }
        }
      }

      guac.getDisplay().showCursor(false);
      guac.getDisplay().oncursor = (canvas, width, height) => {
        displayRootRef.current.style = `cursor: url('${canvas.toDataURL('image/png')}') ${width} ${height}, auto;`;
      };
    }
  }

  /**
   * The mimetype of audio data to be sent along the Guacamole connection if
   * audio input is supported.
   *
   * @constant
   * @type String
   */
  const AUDIO_INPUT_MIMETYPE = 'audio/L16;rate=44100,channels=2';

  /**
   * Requests the creation of a new audio stream, recorded from the user's
   * local audio input device. If audio input is supported by the connection,
   * an audio stream will be created which will remain open until the remote
   * desktop requests that it be closed. If the audio stream is successfully
   * created but is later closed, a new audio stream will automatically be
   * established to take its place. The mimetype used for all audio streams
   * produced by this function is defined by
   * ManagedClient.AUDIO_INPUT_MIMETYPE.
   *
   * @param {Guacamole.Client} client
   *     The Guacamole.Client for which the audio stream is being requested.
   */
  function requestAudioStream(client) {
    // Create new audio stream, associating it with an AudioRecorder
    // let stream = client.createAudioStream(AUDIO_INPUT_MIMETYPE);
    // let recorder = Guacamole.AudioRecorder.getInstance(stream, AUDIO_INPUT_MIMETYPE);

    // // If creation of the AudioRecorder failed, simply end the stream
    // if (!recorder) {
    //     stream.sendEnd();
    // } else {
    //     // Otherwise, ensure that another audio stream is created after this
    //     // audio stream is closed
    //     recorder.onclose = requestAudioStream.bind(this, client);
    // }
  }

  function openConnection(url) {
    getConnectInfo(url).then((response) => {
      if (response.code === 200) {
        const info = response.data;
        // console.log('connect info', info);
        setConnectInfo(info);
        localConnectInfo.current = info;
        document.title = info.resourceName;
        connectToGuacclient(url);
        setConnectCheckFinish(true);
      }
    });
  }

  function checkUrl(url, count) {
    if (count === 0) {
      checkConnect(url).then((response) => {
        if (response.data === 2) {
          checkUrl(url, count + 1);
        } else if (response.code === 200) {
          openConnection(url);
        } else {
          checkUrl(url, count + 1);
        }
      });
    } else {
      setTimeout(() => {
        checkConnect(url).then((response) => {
          if (response.data === 2) {
            if (count < 1000) {
              checkUrl(url, count + 1);
              setErrorMessage(t('Preparing resource...'));
            }
          } else if (response.code === 200) {
            openConnection(url);
          } else {
            if (count < 100) {
              checkUrl(url, count + 1);
            }
          }
        });
      }, 0.3 * 1000);
    }
  }

  function refreshDirectory(file, callback) {
    console.log('connect refreshDirectory', file, fileSystem.current);
    if (fileSystem.current) {
      fileSystem.current.requestInputStream(file, callback);
      return true;
    }
    return false;
  }

  function downloadFile(file) {
    if (fileSystem.current) {
      fileSystem.current.requestInputStream(file);
      return true;
    }
    return false;
  }

  function bufferToBase64(buf) {
    let binstr = Array.prototype.map.call(buf, function (ch) {
      return String.fromCharCode(ch);
    }).join('');
    return btoa(binstr);
  }

  function uploadFile(path, file) {
    if (fileSystem.current) {
      setIsSSHUploading(true);
      const that = this;
      const fileUpload = {};
      
      const STREAM_BLOB_SIZE = 4096;

      //需要读取文件内容，使用filereader
      const reader = new FileReader();
      reader.onloadend = function fileContentsLoaded() {
        //上面源码分析过，这里先创建一个连接服务端的数据通道
        const stream = fileSystem.current.createOutputStream(file.type, path + '/' + file.name);
        const bytes = new Uint8Array(reader.result);

        let offset = 0;
        let progress = 0;

        fileUpload.name = file.name;
        fileUpload.mimetype = file.type;
        fileUpload.length = bytes.length;

        stream.onack = function ackReceived(status) {
          if (status.isError()) {
            //提示错误信息
            //layer.msg(status.message);
            setIsSSHUploadSuccess(false);
            setIsSSHUploading(false);
            return false;
          }

          const slice = bytes.subarray(offset, offset + STREAM_BLOB_SIZE);
          const base64 = bufferToBase64(slice);

          // Write packet
          stream.sendBlob(base64);

          // Advance to next packet
          offset += STREAM_BLOB_SIZE;

          if (offset >= bytes.length) {
            stream.sendEnd();
            refreshDirectory(path);
            setIsSSHUploadSuccess(true);
            setIsSSHUploading(false);
          } 
        }
      };

      reader.readAsArrayBuffer(file);
      return true;
    }
    return false;
  }

  const LoadingMessage = (props) => {
    const { t } = useTranslation();
    const { connectState, tunnelState } = props;
    let externalClass = '';
    let statusString = '';
    if (connectState === 1 || tunnelState === 0) {
      if (errorMessage) {
        statusString = errorMessage;
      } else {
        statusString = t('Connecting');
      }
    } else if (connectState === 2 || tunnelState === 3) {
      statusString = t('Waiting');
    } else if (connectState === 4) {
      if (props.errorMsg) {
        statusString = props.errorMsg;
      } else {
        statusString = t('Disconnecting');
      }
    } else if (connectState === 5 || tunnelState === 2) {
      if (props.errorMsg) {
        statusString = props.errorMsg;
      } else {
        statusString = t('Disconnected!');
      }
    }
    return statusString.length > 0 ? (
      <div className={'loading-message-bg' + ((connectState === 5 || tunnelState === 2) ? ' gray-bg' : '')}>
        <div className='loading-message-root'>
          <div className={'loading-message' + externalClass}>
            {statusString}
          </div>
          {(connectState === 5 || tunnelState === 2) ? <div className='retry-btn' onClick={() => {
            let urlParams = new URLSearchParams(window.location.search);
            let url = urlParams.get('id');
            openConnection(url)
            // window.location.reload();
          }}>{t('Retry')}</div> : null}
        </div>
      </div>
    ) : null;
  };

  useEffect(() => {
    let urlParams = new URLSearchParams(window.location.search);
    let url = urlParams.get('id');
    checkUrl(url, 0);

    aliveConnect(url);
    setInterval(() => {
      aliveConnect(url);
    }, 20000);

    let timer = null;
    window.addEventListener('resize', function() {
      if (!guacClient.current || !displayScale.current) {
        return;
      }
      if (timer) {
        clearTimeout(timer);
      }
      timer = setTimeout(function () {
        const displayAreaHeight = adjustDisplayArea();
        const width = window.innerWidth;
        const height = displayAreaHeight;
        const scale = displayScale.current;
        const display = guacClient.current.getDisplay();
        
        let density = window.devicePixelRatio || 1;
        guacClient.current.sendSize(
          (width * density).toFixed(0), (height * density).toFixed(0));
        display.resize(display.getDefaultLayer(), 
          (width * scale).toFixed(0), (height * scale).toFixed(0));
      }, 400);
    });

    // Fit container within visible region when window scrolls
    window.addEventListener('scroll', fitVisibleArea);

    // Add default destination for input events
    if (!isSafari) {
      let sink = new Guacamole.InputSink();
      document.body.appendChild(sink.getElement());
      sink.getElement().addEventListener('compositionstart', function targetComposeStart(e) {
        composingText = true;
      }, false);

      sink.getElement().addEventListener('compositionend', function targetComposeEnd(e) {
        composingText = false;
      }, false);
    }
  }, []);

  let urlParams = new URLSearchParams(window.location.search);
  return (
    <>
      <div className='container' id='client-container' ref={containerRef}>
            <div className='display-root' id='display-root' ref={displayRootRef}>
              <div className='display' id='display' ref={displayRef} />
            </div>
            <LoadingMessage connectState={connectState} tunnelState={tunnelState} errorMsg={errorMessage}/>
            {
              <FilePanel
                ref={fielPanelRef}
                setClipboard={setClipboard}
                connectCheckFinish={connectCheckFinish}
                refreshDirectory={refreshDirectory}
                directDownloadFile={downloadFile}
                directUploadFile={uploadFile}
                isSSHUploading={isSSHUploading}
                isSSHUploadSuccess={isSSHUploadSuccess}
                remoteFile={remoteFile}
                connectInfo={connectInfo}
              />
            }
            <div
              id='file-button-container-id'
              className='file-button-container'
              onClick={() => {
                changeFilePanelVisible(!filePanelVisible);
              }}
              style={
                filePanelVisible ? (
                  { transform: fileButtonExpandedTransform }
                ) : (
                  { transform: fileButtonCollapseTransform }
                )
              }
            >
              <div className='file-button'>
                {filePanelVisible ? (
                  <img className='file-button-icon' src={FileManagerCollapseIcon} alt='' />
                ) : (
                  <img className='file-button-icon' src={FileManagerExpandIcon} alt='' />
                )}
              </div>
            </div>
            {
              isTouchDevice() && !filePanelVisible && <div>
                <TextInput onKeyDown={textInputOnkeydown} onKeyUp={textInputOnkeyup} textValue={curText} onTextChange={onTextChange} onTextInputFocus={onTextInputFocus} onButtonPressed={onButtonPressed}
                  shiftKeyPressed={shiftKeyPressed} ctrlKeyPressed={ctrlKeyPressed} altKeyPressed={altKeyPressed} tabKeyPressed={tabKeyPressed} escKeyPressed={escKeyPressed}/>
              </div> 
            }
      </div>
    </>
  );
};

export default Connect;
