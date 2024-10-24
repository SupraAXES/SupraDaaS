import React, { useState, useEffect, useRef } from 'react';

import { useTranslation } from 'react-i18next';
import { Modal, Button, Row, Col } from 'antd';

import { SupraInput } from '../input/SupraInput';
import FileManager from './FileManager';
import { 
  getFileList,
  newFolder,
  downloadFile,
  uploadFile,
  deleteFile,
} from '../../api/api';

let FilePanel = (props) => {
  const { t } = useTranslation();
  const {
    connectCheckFinish,
    connectInfo,
    remoteFile,
    setClipboard,
    refreshDirectory,
    directDownloadFile,
    directUploadFile,
    isSSHUploading,
    isSSHUploadSuccess
  } = props;
  const [file, setFileInfo] = useState({ path: '/' });
  const [newFolderName, setNewFolderName] = useState('');
  const [newFolderNameVisible, setNewFolderNameVisible] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [isUploadFileSuccess, setIsUploadFileSuccess] = useState(false);

  const linkRef = useRef();

  const listFolder = (url, path) => {
    if (!refreshDirectory(path)) {
      getFileList(url, path).then(data => {
        if (data && data.code === 200) {
          setFileInfo({files: data.data, path: path});
        }
      });
    }
  }

  useEffect(() => {
    let urlParams = new URLSearchParams(window.location.search);
    let url = urlParams.get('id');
    if (connectCheckFinish) {
      listFolder(url, (remoteFile || file).path);
    }
  }, [(remoteFile || file).path, connectCheckFinish]);

  useEffect(() => {
    if (file.download && linkRef) {
      let path = file.download.config.params.path;
      let fileName = path.substring(path.lastIndexOf('/') + 1);
      const href = window.URL.createObjectURL(file.download.data);
      const a = linkRef.current;
      a.download = fileName;
      a.href = href;
      a.click();
    }
  }, [file.download]);

  let urlParams = new URLSearchParams(window.location.search);
  let url = urlParams.get('id');
  const newFolderFooter = [
    <Button
      key='cancel'
      onClick={() => {
        setNewFolderName('');
        setNewFolderNameVisible(false);
      }}
    >
      {t('cancel')}
    </Button>,
    <Button
      key='save'
      type='primary'
      onClick={() => {
        if (newFolderName.length === 0) {
          return;
        }
        // Create folder
        let newFolderDir = (remoteFile || file).path + '/' + newFolderName;
        if ((remoteFile || file).path === '/') {
          newFolderDir = '/' + newFolderName;
        }
        newFolder(url, newFolderDir).then(data => {
          if (data && data.code === 200) {
            listFolder(url, (remoteFile || file).path);
          }
        });
        setNewFolderName('');
        setNewFolderNameVisible(false);
      }}
    >
      {t('confirm')}
    </Button>
  ];
  
  return (
    <div id='file-panel' className='file-panel-container'>
      <Row>
        <Col span={12}>
          <FileManager
            setClipboard={setClipboard}
            isUploading={isUploading}
            isUploadFileSuccess={isUploadFileSuccess}
            isSSHUploading={isSSHUploading}
            isSSHUploadSuccess={isSSHUploadSuccess}
            files={(remoteFile || file).files || []}
            path={(remoteFile || file).path}
            setNewFolderNameVisible={setNewFolderNameVisible}
            uploadFile={(id, path, tmpFile) => {
              if (!directUploadFile(path, tmpFile)) {
                setIsUploading(true);
                setIsUploadFileSuccess(false);
                uploadFile(id, path, [tmpFile]).then(data => {
                  setIsUploading(false);
                  if (data && data.code === 200) {
                    setIsUploadFileSuccess(true);
                    listFolder(url, path);
                  }
                });
              }
            }}
            connectInfo={connectInfo}
            handlePathSelect={(filePath) => {
              listFolder(url, filePath);
            }}
            handleDownload={(path) => {
              if (!directDownloadFile(path)) {
                downloadFile(url, path);
              }
            }}
            handleDelete={(filePath) => {
              if (!(connectInfo.protocol === 'SSH')) {
                deleteFile(url, filePath);
              }
            }}
            destroyOnClose={true}
          />
        </Col>
      </Row>
      <a ref={linkRef}></a>
      {newFolderNameVisible && (
        <Modal
          className='new-folder-modal'
          visible={newFolderNameVisible}
          footer={newFolderFooter}
          width='250px'
          closable={false}
          maskClosable={false}
          getContainer={() => document.getElementById('file-panel')}
          wrapClassName='file-manager-modal'
          onCancel={() => {
            setNewFolderNameVisible(false);
          }}
          destroyOnClose={true}
          style={{ right: '300px' }}
        >
          <SupraInput
            className='new-folder-name-input'
            value={newFolderName}
            handleChange={setNewFolderName}
          />
        </Modal>
      )}
    </div>
  );
};

export default FilePanel;
