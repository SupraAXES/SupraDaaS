import React, { useEffect, useState } from 'react';
import { Layout, Col, Row } from 'antd';
import { useTranslation } from 'react-i18next';

import AppGroup from '../../components/AppGroup/AppGroup';
import { getHomeResources } from '../../api/api';
import EmptyIcon from '../../assets/image/empty.png';
import Logo from '../../assets/image/logo.png';

import './Home.css';

const groupedResources = (resources) => {
    let resourceGroups = [];
    let groupMap = new Map();
    resources.forEach((r) => {
        if (!r.group) {
            r.group = 'Default';
        }

        let group = groupMap.get(r.group);
        if (group === undefined) {
            group = {
                title: r.group,
                resources: [r]
            };
            groupMap.set(r.group, group);
            resourceGroups.push(group);
        } else {
            group.resources.push(r);
        }
    });
    return resourceGroups.sort((a, b) => a.title.localeCompare(b.title))
}

const Home = () => {
    const { t } = useTranslation();
    const [resourceGroups, setResourceGroups] = useState([]);

    useEffect(() => {
        document.title = 'Supra';
        getHomeResources().then(data => {
            if (data && data.code === 200) {
                setResourceGroups(groupedResources(data.data));
            }
        });
    }, []);

    return <Layout className='content-container'>
        <div className="logo-container">
            <a href="https://www.supraaxes.cn/" target="_blank">
                <img src={Logo} className="logo"/>
            </a>
        </div>
        {resourceGroups && resourceGroups.length > 0 ? (
            resourceGroups.map((g) => (
                <AppGroup
                    key={g.title}
                    title={g.title}
                    items={g.resources}
                />
            ))
            ) : (
            <Row justify='center' className='empty-container'>
                <Col className='empty-place-holder'>
                <img src={EmptyIcon} className='empty-image' alt='' />
                <div className='empty-message'>{t('No content message')}</div>
                </Col>
            </Row>
            )}
    </Layout>
}

export default Home;
