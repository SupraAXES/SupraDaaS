import React, { useEffect } from 'react';
import { message } from 'antd';
import { openResource } from '../../api/api';

const TempConnect = (props) => {
  const [messageApi, messageHolder] = message.useMessage();

  const urlParams = new URLSearchParams(window.location.search);
  const resourceId = urlParams.get('id');

  const onConnect = (resource) => {
    openResource(resource).then(data => {
      if (data && data.code === 200) {
        window.location.href = `/connect/?id=${data.data}`;
        window.history.pushState(null, '', window.location.href);
      } else {
        messageApi.open({ type: 'error', content: '连接失败' });
      }
    });
  };

  const isTouchDevice = () => {
    return 'ontouchstart' in document.documentElement;
  };

  useEffect(() => {
    const resourceInfoStr = localStorage.getItem(resourceId);
    localStorage.removeItem(resourceId);
    
    const windowSize = {
      width: window.innerWidth,
      height: Math.floor(window.innerHeight - (isTouchDevice() ? 36 : 0))
    }
    let resourceInfo;
    if (resourceInfoStr) {
      resourceInfo = JSON.parse(resourceInfoStr);
      onConnect({
        id: resourceId,
        address: resourceInfo.address,
        autoAccounts: resourceInfo.autoAccounts,
        protocol: resourceInfo.protocol,
        windowSize
      })
    } else {
      onConnect({
        id: resourceId,
        windowSize
      })
    }
  }, []);

  return <div>{messageHolder}</div>
}

export default TempConnect;
