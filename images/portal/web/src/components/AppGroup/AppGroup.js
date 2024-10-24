import React, { useState } from 'react';
import { Row, Col, Modal } from 'antd';
import { Link } from 'react-router-dom';
import { trucateInBegin, trucateInMiddle } from '../../common/util.js';
import LoginPopover from './LoginPopover.js';

import './AppGroup.css';

let AppGroup = (props) => {
  const { title, items } = props;
  const [ showLogin, setShowLogin ] = useState(false);
  const [ item, setItem ] = useState(null);

  return (
    <>
    <div className='group-container'>
      <Row className='group-title-container'>
        <Col className='group-title-item'>
          <div className='group-title-icon' />
        </Col>
        <Col className='group-title-item'>
          <span className='group-title'>{title}</span>
        </Col>
      </Row>
      <Row className='group-item-container' gutter={[50, 30]} justify='start'>
        {items.map((item, index) => {
          const iconEl = item.icon ? (
            <img src={item.icon} alt='' className='app-image' />
            ) : (
              <div className='app-default-icon'>{trucateInBegin(item.name, 4)}</div>
            );
          const titleEl = <div className='app-name'>{trucateInMiddle(item.name, 8, 8)}</div>;
          if (item.loginRequired) {
            return <Col className='item-container' key={index}>
              <div className={'app-button'} onClick={() => {
                setItem(item);
                setShowLogin(true);
              }}>
                {iconEl}
              </div>
              {titleEl}
            </Col>
          }
          return (
          <Col className='item-container' key={index}>
            <Link className={'app-button'} target='_blank' to={`/temp?id=${item.id}`}>
              {iconEl}
            </Link>
            {titleEl}
          </Col>)
        })
        }
      </Row>
    </div>
    {showLogin ? <Modal 
      title={item ? item.name : ''} 
      onCancel={
        () => {
          setShowLogin(false);
        }
      }
      footer={<></>}
      width={350}
      open
      visible
      centered
      className='connect-popover'
      >
        <LoginPopover item={item} onSubmit={
          (values) => {
            localStorage.setItem(item.id, JSON.stringify(values));
            setShowLogin(false);
          }
        }></LoginPopover>
      </Modal> : null
      }
    </>
  );
};

export default AppGroup;