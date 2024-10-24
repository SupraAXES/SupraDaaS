import React, { useState } from "react";
import { useTranslation } from 'react-i18next';
import { 
    ProForm, 
    ProFormText, 
    ProFormSelect, 
    ProFormList,
    ProFormCheckbox,
    ProFormDependency,
} from "@ant-design/pro-components";
import { DownOutlined, UpOutlined } from '@ant-design/icons';
import { SupraFormTextPassword, SupraFormText, SupraFormTextArea } from "../../components/input/SupraInput";

const UpdateForm = (props) => {
    const { t } = useTranslation();
    const [showMoreAttributes, setShowMoreAttributes] = useState(false);

    const vmSettings = props.vmSettings || { templates: [], cpuSizes: [], memorySizes: [], diskSizes: []};
    const resource = props.resource || {};

    return (
        <div style={{overflowY: 'scroll', maxHeight: 540}}>
        <ProForm form={props.form} submitter={false}>
            <ProForm.Group>
                <ProFormText name="name" label={t('resource name')} width="md"
                    rules={[{ required: true, message: t('please enter') + t('resource name')}]}
                    initialValue={resource.name}/>
                <ProFormText name="group" label={t('group')} width="md"
                    initialValue={resource.group}/>
            </ProForm.Group>
            <ProForm.Group>
                <ProFormSelect name="templateId" label={t('template')} width="md"
                    options={vmSettings.templates.map(v => ({value: v.id, label: v.name}))} 
                    rules={[{ required: true, message: t('please select') + t('template')}]}
                    initialValue={resource.templateId}/>
            </ProForm.Group>
            <ProForm.Group>
                <ProFormSelect name="cpuSize" label={t('cpu core')} width={148}
                    options={vmSettings.cpuSizes.map(v => ({value: v, label: v}))} 
                    rules={[{ required: true, message: t('please select') + t('cpu core') }]}
                    initialValue={resource.vmOpts?.cores}/>
                <ProFormSelect name="memorySize" label={t('memory')} width={148}
                    options={vmSettings.memorySizes.map(v => ({value: v, label: v}))} 
                    rules={[{ required: true, message: t('please select') + t('memory') }]}
                    initialValue={resource.vmOpts?.ram}/>
                <ProFormSelect name="diskSize" label={t('disk')} width={148}
                    options={vmSettings.diskSizes.map(v => ({value: v, label: v}))} 
                    rules={[{ required: true, message: t('please select') + t('disk') }]}
                    initialValue={resource.diskSizes}/>
            </ProForm.Group>
            <ProFormDependency name={['templateId']}>
                {({ templateId }) => {
                    const template = vmSettings.templates.find((v) => v.id === templateId);
                    if (template && template.userMgr) {
                        return (
                            <>
                                <ProForm.Group>
                                    <ProFormText name="username" label={t('admin user')} width="md"
                                        initialValue={resource.username || template.admin}/>
                                    <ProFormText.Password name="password" label={t('admin password')} width="md"
                                        initialValue={resource.password}/>
                                </ProForm.Group>
                                <ProFormList name="autoAccounts"
                                    rules={[
                                        {
                                        validator: (rule, v, callback) => {
                                            if (v) {
                                            let defCount = 0;
                                            v.forEach((item) => {
                                                if (item && item.def) {
                                                defCount += 1;
                                                }
                                            });
                                            if (defCount > 1) {
                                                callback(t('default account') + t('only one allowed'));
                                            }
                                            }
                                            callback();
                                        },
                                        },
                                    ]}
                                    creatorButtonProps={{creatorButtonText: t('new account')}}
                                    initialValue={resource.autoAccounts}
                                >
                                    <ProForm.Group>
                                        <SupraFormText
                                            label={t('username')}
                                            name="user"
                                            width={148}
                                        />
                                        <SupraFormTextArea
                                            label={t('private key')}
                                            name="pri"
                                            width={148}
                                            fieldProps={{ rows: 1 }}
                                        />
                                        <SupraFormTextPassword
                                            label={t('password')}
                                            name="pass"
                                            width={148}
                                        />
                                        <ProFormCheckbox
                                            label={t('default account')}
                                            name="def"
                                            width="xs"
                                        />
                                    </ProForm.Group>
                                </ProFormList>
                            </>
                        );
                    }

                    if (template && !template.userMgr && template.admin) {
                        return <ProForm.Group>
                            <ProFormText name="username" label={t('admin user')} width="md" disabled
                                initialValue={template.admin}/>
                            <ProFormText.Password name="password" label={t('admin password')} width="md"
                                initialValue={resource.password}/>
                        </ProForm.Group>
                    }

                    return <></>
                }}
            </ProFormDependency>
            <ProFormList
                name="access"
                rules={[
                    {
                    validator: (rule, v, callback) => {
                        if (v) {
                        let defCount = 0;
                        v.forEach((item) => {
                            if (item && item.def) {
                            defCount += 1;
                            }
                        });
                        if (defCount > 1) {
                            callback(t('default access') + t('only one allowed'));
                        }
                        }
                        callback();
                    },
                    },
                ]}
                creatorButtonProps={{creatorButtonText: t('new access')}}
                initialValue={resource.access || []}
            >
                <ProForm.Group>
                    <ProFormSelect
                        label={t('access method')}
                        name="pl"
                        width="sm"
                        options={['ssh', 'rdp', 'vnc', 'guest_vnc'].map(
                            (v) => {
                                let label = v.toUpperCase();
                                if (v === 'guest_vnc') {
                                    label = t('virtual terminal');
                                }
                                return { label, value: v };
                            },
                        )}
                    />
                    <SupraFormText label={t('port')} name="port" width="sm"/>
                    <ProFormCheckbox label={t('default access')} name="def" width="xs"/>
                </ProForm.Group>
            </ProFormList>
            <div key="more" style={{ paddingBottom: 10 }}>
                <a onClick={() => setShowMoreAttributes(!showMoreAttributes)}>
                    {t('more attributes')}
                    {!showMoreAttributes && <DownOutlined />}
                    {showMoreAttributes && <UpOutlined />}
                </a>
            </div>
            {showMoreAttributes && 
                <>
                <ProForm.Group>
                    <SupraFormText
                        label={t('ip/mask')}
                        rules={[
                        {
                            validator: (rule, v, callback) => {
                            if (v && !v.match(/^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\/\d{1,2}$/)) {
                                callback(t('ip/mask') + t('incorrect'));
                            }
                            callback();
                            },
                        },
                        ]}
                        width="md"
                        name="ipMask"
                        disabled={resource.id ? true : false}
                        initialValue={resource.ipMask}
                    />
                    <SupraFormText label={t('mac')} width="md"
                        rules={[
                        {
                            validator: (rule, v, callback) => {
                                if (v && !v.match(/^([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}$/)) {
                                    callback(t('mac') + t('incorrect'));
                                }
                                callback();
                            },
                        },
                        ]}
                        name="mac"
                        disabled={resource.id ? true : false}
                        initialValue={resource.mac}
                    />
                    </ProForm.Group>
                    <ProForm.Group>
                        <ProFormCheckbox name="netRestrict" width="md"
                            label={t('network restriction')}
                            initialValue={resource.netRestrict}
                        />
                    </ProForm.Group>
                </>
            }
        </ProForm>
        </div>
    );
}

export default UpdateForm;