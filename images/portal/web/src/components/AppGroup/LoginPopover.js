import React, { useState } from 'react';
import { Form, Button, Row, Col, Select } from 'antd';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { trucateInBegin } from '../../common/util.js'
import './LoginPopover.css'
import { SupraInput, SupraInputPassword } from '../input/SupraInput.js';

const LoginPopover = (props) => {
    const { t } = useTranslation();
    const { item, onSubmit } = props;  
    const [protocol, setProtocol] = useState(
        item.access && item.access.length > 0 ? item.access[0] : '');
    const [address, setAddress] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const validateValues = () => {
        const needUser = item.loginRequired && protocol.indexOf('http') < 0 && protocol !== 'guest_vnc';
        if (needUser && username.length === 0 && password.length === 0) {
            return null;
        }
        if (item.addressRequired && address.length === 0) {
            return null;
        }

        let value = { id: item.id };
        if (needUser) {
            value.autoAccounts = [{ user: username, pass: password }];
        }
        if (item.addressRequired) {
            value.address = address;
        }
        if (item.access) {
            value.protocol = protocol;
        }
        return value;
    }

    const onFinish = () => {
        const allValues = validateValues();
        if (allValues) {
            onSubmit(allValues);
            window.open(`/temp?id=${item.id}`, '_blank')
            return true;
        }
        return false;
    }

    const needUser = item.loginRequired && protocol.indexOf('http') < 0 && protocol !== 'guest_vnc';
    
    return (
        <Form onFinish={onFinish} autoComplete='false'>
            <Row gutter={[16, 16]}>
                <Col span={8}>
                    {
                        item.icon ? (<img src={item.icon} alt='' className='app-image'></img>)
                            : (<div className='connect-default-icon'>{trucateInBegin(item.name, 4)}</div>)
                    }
                </Col>
                <Col span={16}>
                    {item.addressRequired ? <Row>
                        <SupraInput onChange={(event) => {
                            setAddress(event.target.value);
                        }}
                            placeholder={t('please enter') + t('address')}
                            style={{ marginBottom: 16 }}
                        />
                    </Row> : <></>}
                    {item.access && item.access.length > 0 ? <Row>
                        <Select onChange={(event) => {
                            setProtocol(event);
                        }}
                        style={{ marginBottom: 16, width: '100%', borderRadius: 6 }}
                        options={item.access.map(v => {
                            const tmp = v.split(':');
                            if (tmp.length > 1) {
                                let label = tmp[0].toUpperCase();
                                if (label === 'guest_vnc') {
                                    label = t('virtual terminal');
                                }
                                return { label: label + '(' + tmp[1] + ')', value: v };
                            }
                            return { label: tmp[0].toUpperCase(), value: v };
                        })}
                        defaultValue={item.access[0]}
                        />
                    </Row> : <></>}
                    {needUser ? <Row gutter={[16, 16]}>
                        <Col span={24}>
                            <SupraInput handleChange={(value) => {
                                setUsername(value);
                            }}
                                placeholder='请输入用户名'
                            />
                        </Col>
                        <Col span={24}>
                            <SupraInputPassword handleChange={(value) => {
                                setPassword(value);
                            }}
                                placeholder='请输入密码'
                            />
                        </Col>
                    </Row> : <></>}
                </Col>
            </Row>
            <Row>
                <Col span={24}>
                    <Button block
                        type='primary'
                        htmlType='submit'
                        shape='round'
                        style={{ marginTop: 16 }}
                    >
                        <Link onClick={(event) => {
                            const allValues = validateValues();
                            if (allValues) {
                                onSubmit(allValues);
                                return true;
                            } else {
                                event.preventDefault();
                                return false;
                            }
                        }} target='_blank' to={`/temp?id=${item.id}`}>
                            连接
                        </Link>
                    </Button>
                </Col>
            </Row>
        </Form>
    )
}

export default LoginPopover;
