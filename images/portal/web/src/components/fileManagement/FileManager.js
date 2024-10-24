import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import {
  InboxOutlined,
  SearchOutlined,
  LoadingOutlined,
  FolderAddOutlined,
  FileOutlined,
  CloseOutlined,
  MenuOutlined
} from '@ant-design/icons';
import { Breadcrumb, Modal, Row, Col, Button, Upload, Popover } from 'antd';

import './FileManager.css';
import FolderIcon from '../../assets/image/folder.png';
import RefreshIcon from '../../assets/image/refresh_gray.png';
import FileListFolderIcon from '../../assets/image/file-list-folder.png';
import { SupraInput, SupraInputTextArea } from '../input/SupraInput';

const { Dragger } = Upload;

let FileManager = (props) => {
  const { 
    setNewFolderNameVisible, 
    uploadFile, 
    isSSHUploading, 
    isSSHUploadSuccess, 
    isUploading, 
    isUploadFileSuccess, 
    setClipboard,
    connectInfo
  } = props;
  const { t } = useTranslation();
  const { files, path, handleDownload, handleDelete, handlePathSelect, handleOpenPath } = props;
  const [displayFiles, setDisplayFiles] = useState(files || []);
  let urlParams = new URLSearchParams(window.location.search);
  let url = urlParams.get('id');
  let pathItems = ['/'];
  let pathKeys = [];
  if (path && path.length > 0) {
    pathKeys = path.split('/').filter((key) => {
      return key.length > 0;
    });
  }
  for (let index = 0; index < pathKeys.length; ++index) {
    pathItems.push(pathKeys[index] + '/');
  }

  useEffect(() => {
    const defaultFilesList = [...files];
    setDisplayFiles(defaultFilesList)
  }, [files]);
  const uploadProps = {
    className: 'upload-dragger',
    name: '',
    action: '',
    headers: {},
    beforeUpload: (f) => {
      uploadFile(url, path, f);
      return false;
    },
    showUploadList: false
  };

  const UseRemoteFile = () => connectInfo.protocol === 'SSH';

  const hasFolderOperation = () => {
    if (!UseRemoteFile() && connectInfo.upload) {
      return true;
    }
    return false;
  }

  const getFolderOperationContent = (file) => {
    if (UseRemoteFile()) {
      return null;
    }
    return (
      <div className='operation-container'>
        {!UseRemoteFile() && connectInfo.upload && <div
          key={4}
          span={3}
          className='operation-item'
          onClick={(event) => {
            event.preventDefault();
            event.stopPropagation();
            // download file
            Modal.confirm({
              title: t('confirm'),
              content:
                  t('Are you sure to delete selected file ') + file.name,
              okText: t('confirm'),
              cancelText: t('cancel'),
              onOk: () => {
                let filePath = path + '/' + file.name;
                if (path === '/') {
                  filePath = '/' + file.name;
                }
                if (handleDelete) {
                  handleDelete(filePath);
                }
              }
            });
          }}
        >
          {t('delete')}
        </div>}
      </div>
    )
  }

  const hasFileOperation = () => {
    if (connectInfo.download) {
      return true;
    }
    if (!UseRemoteFile() && connectInfo.upload) {
      return true;
    }
    return false;
  }

  const getFileOperationContent = (file) => {
    return (
      <div className='operation-container'>
        { connectInfo.download && <div
          key={4}
          span={3}
          className='operation-item'
          onClick={(event) => {
            event.preventDefault();
            event.stopPropagation();
            // download file
            Modal.confirm({
              title: t('confirm'),
              content:
                  t('Are you sure to download selected file ') + file.name,
              okText: t('confirm'),
              cancelText: t('cancel'),
              onOk: () => {
                let filePath = path + '/' + file.name;
                if (path === '/') {
                  filePath = '/' + file.name;
                }
                if (handleDownload) {
                  handleDownload(filePath);
                }
              }
            });
          }}
        >
          {t('download')}
        </div>}
        {!UseRemoteFile() && connectInfo.upload && <div
          key={5}
          span={3}
          className='operation-item'
          onClick={(event) => {
            event.preventDefault();
            event.stopPropagation();
            // download file
            Modal.confirm({
              title: t('confirm'),
              content:
                  t('Are you sure to delete selected file ') + file.name,
              okText: t('confirm'),
              cancelText: t('cancel'),
              onOk: () => {
                let filePath = path + '/' + file.name;
                if (path === '/') {
                  filePath = '/' + file.name;
                }
                if (handleDelete) {
                  handleDelete(filePath);
                }
              }
            });
          }}
        >
          {t('delete')}
        </div>}
      </div>
    )
  }

  return (
    <div className='file-manager-container' id='file-manager'>
      {connectInfo.paste && <><div className='connection-intro'>
        <div className='connection-brand'>{connectInfo.resourceName}</div>
        <div className='clipboard-section'>
          <SupraInputTextArea
            id='clipboard-input'
            placeholder={t('clipBoard')}
            handleChange={setClipboard}
          />
        </div>
      </div>
      <div className='separator'/>
      </>}
      <div className='file-manager-header-title'>
        <img className='title-folder-icon' src={FolderIcon} alt=''/>
        {((connectInfo.view || connectInfo.edit) && !UseRemoteFile()) ? <a className='file-manager-header-link' 
          href='' onClick={(e) => {
            e.preventDefault(); 
            handleOpenPath('/');
          }}>{t('file manager')}</a> : <span className='file-manager-header-text'>{t('file manager')}</span>}
        <img
          className='title-refresh-icon'
          src={RefreshIcon}
          alt=''
          onClick={() => {
            handlePathSelect(path);
          }}
        />
      </div>
      <div>
        <SupraInput
          prefix={<SearchOutlined className='file-search-icon' />}
          className='file-search'
          placeholder={t('Enter key word')}
          handleChange={(searchKey) => {
            const filterdFiles = files.filter((file) => {
              return file.name.indexOf(searchKey) !== -1;
            });
            setDisplayFiles(filterdFiles);
          }}
        />
      </div>
      <div className='file-path'>
        <span>
          <Breadcrumb separator=''>
            {pathItems.map((item, index) => {
              return (
                <Breadcrumb.Item
                  key={index}
                  onClick={() => {
                    if (index >= pathItems.length - 1) {
                      return;
                    }
                    let path = '/';
                    for (let i = 1; i <= index; ++i) {
                      path += pathItems[i];
                      path += '/';
                    }
                    handlePathSelect(path);
                  }}
                >
                  <a className='path-item'>{item}</a>
                </Breadcrumb.Item>
              );
            })}
          </Breadcrumb>
        </span>
      </div>
      <div className='file-list-container'>
        {
          displayFiles.length === 0 && <div className='file-list-empty'>{t('No Folder or File')}</div>
        }
        {displayFiles.filter(file => {
          return file.isDirectory === true; 
        }).map((file, index) => {
          return (
            <Row className='file-list-row folder-row' key={'f' + index}>
              <Col key={1} span={4}>
                {file.isDirectory ? (
                  <img className='folder-icon' src={FileListFolderIcon} alt=''/>
                ) : (
                  <FileOutlined className='file-icon'/>
                )}
              </Col>
              <Col key={2} span={18} className={file.isDirectory ? 'folder-name' : 'file-name'} onClick={
                (event) => {
                  event.preventDefault();
                  event.stopPropagation();
                  if (file.isDirectory) {
                    let filePath = path + '/' + file.name;
                    if (path === '/') {
                      filePath = '/' + file.name;
                    }
                    if (handlePathSelect) {
                      handlePathSelect(filePath);
                    }
                  }
                }
              }>
                {file.name}
              </Col>
              {hasFolderOperation() && <Popover content={getFolderOperationContent(file)} placement='bottom' trigger='click'>
                <Col 
                  key={3}
                  span={2}
                  className='file-operation'>
                  <MenuOutlined/>
                </Col>
              </Popover>}
            </Row>
          );
        })}
        {displayFiles.filter(file => {
          return file.isDirectory === false; 
        }).map((file, index) => {
          return (
            <Row className='file-list-row' key={'df' + index}>
              <Col key={1} span={4}>
                {file.isDirectory ? (
                  <img className='folder-icon' src={FileListFolderIcon} alt=''/>
                ) : (
                  <FileOutlined className='file-icon'/>
                )}
              </Col>
              <Col key={2} span={18} className={file.isDirectory ? 'folder-name' : 'file-name'} onClick={
                (event) => {
                  event.preventDefault();
                  event.stopPropagation();
                  if (file.isDirectory) {
                    let filePath = path + '/' + file.name;
                    if (path === '/') {
                      filePath = '/' + file.name;
                    }
                    if (handlePathSelect) {
                      handlePathSelect(filePath);
                    }
                  }
                }
              }>
                {file.name}
              </Col>
              { hasFileOperation() && <Popover content={getFileOperationContent(file)} placement='bottom' trigger='click'>
                <Col key={3} span={2} className='file-operation'>
                  <MenuOutlined/>
                </Col>
              </Popover>}
            </Row>
          );
        })}
      </div>
      <div>
      </div>
      <div className='button-container'>
        <div className='separator'></div>
        {connectInfo.upload && <div className='upload-dragger-container'>
          <Dragger {...uploadProps} disabled={isUploading || isSSHUploading}>
            {
              (isUploading || isSSHUploading)
                ? <p className='ant-upload-drag-icon'>
                  <LoadingOutlined />
                </p> 
                : (
                  (isUploadFileSuccess || isSSHUploadSuccess)
                  ? <p className='ant-upload-drag-icon'>
                    <InboxOutlined />
                  </p> 
                  : <p className='ant-upload-drag-icon'>
                    <CloseOutlined />
                  </p>
                )
            }
            {(isUploadFileSuccess || isSSHUploadSuccess) ? <p className='upload-text'>{t('Upload Hint Message')}</p> : <p className='upload-text'>{t('Upload Fail Message')}</p>}
          </Dragger>
        </div>}
        {!(connectInfo.protocol === 'SSH') && connectInfo.upload && <div>
          <Button
            className='new-folder-button'
            icon={<FolderAddOutlined />}
            type='primary'
            onClick={() => {
              setNewFolderNameVisible(true);
            }}>
            {t('New Folder')}
          </Button>
        </div>}
      </div>
    </div>
  );
};

export default FileManager;
