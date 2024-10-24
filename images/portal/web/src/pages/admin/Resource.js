import React, { useState, useEffect } from "react";
import { Modal, message, Layout, Menu, theme, Table, Button, Form } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import { 
    createResource, 
    deleteResource, 
    getResourceList, 
    getResourceVmSettings, 
    updateResource,
    getVmStatus,
    vmOperate,
} from "../../api/api";
import UpdateForm from "./UpdateForm";

import Logo from '../../assets/image/logo.png';
import './Resource.css';

const { Header, Content, Footer, Sider } = Layout;

const formToResource = (oldValues, values) => {
    const resource = {...oldValues, ...values};
    resource.vmOpts = {
        cores: resource.cpuSize,
        ram: resource.memorySize,
        mac: resource.mac,
        guest_restrict: resource.netRestrict ? true : false,
    };
    return resource;
};

const Resource = (props) => {
    const { t } = useTranslation();
    const [modalApi, modalHolder] = Modal.useModal();
    const [messageApi, messageHolder] = message.useMessage();
    const [form] = Form.useForm();

    const [vmSettings, setVmSettings] = useState({});
    const [resourceList, setResourceList] = useState([]);
    const {
        token: { colorBgContainer, borderRadiusLG },
    } = theme.useToken();

    const [selectResource, setSelectResource] = useState(null);

    const refreshResourceList = () => {
        getResourceList().then((res) => {
            if (res && res.code === 200) {
                setResourceList(res.data);
            }
        });
    }

    const refreshVmStatus = () => {
        getVmStatus(selectResource.id).then((res) => {
            if (res && res.code === 200) {
                const status = res.data;
                modalApi.confirm({
                    width: 600,
                    title: t('resource status') + '：' + selectResource.name,
                    content: <div style={{ marginTop: 20 }}>
                    {t('vm status')}：
                    <span>
                      {t('device')}{status.qemu ? status.qemu : 'Not Availble'}
                    </span>
                    <span style={{ marginLeft: 6, marginRight: 10 }}>
                      {t('access')}{status.vm ? status.vm : 'Not Availble'}
                    </span>
                    {status.qemu || status.vm ? (
                      <Button type="link" onClick={() => handleVmOperate(selectResource?.id || '', 'stop')}>
                        {t('force stop')}
                      </Button>
                    ) : (
                      <Button type="link" onClick={() => handleVmOperate(selectResource.id || '', 'run')}>
                        {t('start')}
                      </Button>
                    )}
                  </div>,
                });
            }
        });
    }

    useEffect(() => {
        document.title = t('vm admin');
        refreshResourceList();
        getResourceVmSettings().then((res) => {
            if (res && res.code === 200) {
                setVmSettings(res.data);
            }
        });
    }, []);

    useEffect(() => {
        if (selectResource) {
            refreshVmStatus();
        }
    }, [selectResource]);

    const showToast = (type, content) => {
        messageApi.open({
            type: type,
            content: content,
            duration: 2,
        });
    }

    const showCreateModal = () => {
        const content = <UpdateForm form={form} vmSettings={vmSettings} resource={{}}/>;
        if (form) {
            form.resetFields();
        }
        modalApi.confirm({
            title: t('create resource'),
            content: content,
            width: 800,
            onOk() {
                createResource(formToResource({}, form.getFieldsValue())).then((res) => {
                    if (res && res.code === 200) {
                        refreshResourceList();
                        showToast('success', t('operate success'));
                    }
                    else {
                        showToast('error', t('operate failed') + `：${res.msg}`);
                    }
                })
            }
        });
    }

    const showUpdateModal = (resource) => {
        const content = <UpdateForm form={form} vmSettings={vmSettings} resource={resource}/>;
        if (form) {
            form.resetFields();
        }
        modalApi.confirm({
            title: t('update resource'),
            content: content,
            width: 800,
            onOk() {
                updateResource(formToResource(resource, form.getFieldsValue())).then((res) => {
                    if (res && res.code === 200) {
                        refreshResourceList();
                        showToast('success', t('operate success'));
                    } else {
                        showToast('error', t('operate failed') + `：${res.msg}`);
                    }
                });
            },
            open() {
                form.resetFields();
            }
        });
    }

    const showDeleteModal = (resource) => {
        modalApi.confirm({
            title: '删除资源',
            content: `确定删除资源 ${resource.name} 吗？`,
            onOk() {
                deleteResource(resource).then((res) => {
                    if (res && res.code === 200) {
                        refreshResourceList();
                        showToast('success', t('operate success'));
                    } else {
                        showToast('error', t('operate failed') + `：${res.msg}`);
                    }
                });
            }
        });
    }

    const handleVmOperate = (id, action) => {
        vmOperate(id, action).then((resp) => {
            if (resp && resp.code === 200) {
              message.success(t('operate success'));
            } else {
              message.error(t('operate failed'));
            }
        });
    };

    const showStatusModal = (resource) => {
        message.success(t('querying status'));
        setSelectResource({...resource});
    };

    const columns = [
        {
            title: t('resource name'),
            dataIndex: 'name',
            key: 'name',
        },
        {
            title: t('group'),
            dataIndex: 'group',
            key: 'group',
        },
        {
            title: t('create time'),
            dataIndex: 'createTime',
            key: 'createTime',
            render: (dom) => new Date(dom * 1000).toLocaleString(),
        },
        {
            title: t('status'),
            dataIndex: 'status',
            key: 'status',
            render: (dom, item) => dom === 1 ? t('Disable'): t('Enable'),
        },
        {
            title: t('Running Status'),
            dataIndex: 'vmStatus',
            key: 'vmStatus',
            render: (dom, item) => <Button type="link" onClick={() => showStatusModal(item)} style={{marginLeft: -15}}>{t('View Status')}</Button>,
        },
        {
            title: t('options'),
            dataIndex: 'options',
            key: 'options',
            render: (dom, item) => (<>
                <Button type="link" onClick={() => showUpdateModal(item)} style={{marginLeft: -15}}>{t('update')}</Button>
                <Button type="link" onClick={() => showDeleteModal(item)}>{t('delete')}</Button>
            </>)
        }
    ];

    return <Layout style={{height: "100vh"}}>
        {/* <Sider breakpoint="lg" collapsedWidth="0">
            <Menu theme="dark" mode="inline" 
                items={[{key: "menu0", label: t('resource admin')}]}
                selectedKeys={["menu0"]} />
        </Sider> */}
        <Layout>
        <Header style={{ padding: 0, background: colorBgContainer }} >
            <div className="logo-container">
                <a href="https://www.supraaxes.cn/" target="_blank">
                    <img src={Logo} className="logo"/>
                </a>
            </div>
        </Header>
        <Content style={{ margin: '24px 16px 0' }}>
            <div
                style={{
                    padding: 24,
                    minHeight: 360,
                    textAlign: 'right',
                    background: colorBgContainer,
                    borderRadius: borderRadiusLG,
                }}
            >   
                <Button type='primary' icon={<PlusOutlined />} 
                    style={{marginBottom: 10}}
                    onClick={showCreateModal}>
                    {t('create resource')}
                </Button>   
                <Table rowKey='name'
                    dataSource={resourceList} 
                    columns={columns}/>
            </div>
        </Content>
        <Footer style={{ textAlign: 'center' }}>
            Supra
        </Footer>
        </Layout>
        <div>{modalHolder}</div>
        <div>{messageHolder}</div>
    </Layout>
};

export default Resource;