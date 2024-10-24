import qs from 'querystringify';

const fetchRequest = (url, param = null, config = {}) => {
    if (config.method === 'POST' && !config.headers) {
        config.headers = {
            'Content-Type': 'application/json'
        };
    }
    if (param) {
        url += '?' + qs.stringify(param);
    }
    return fetch(url, config).then(response => response.json());
}

export const getHomeResources = () => {
    return fetchRequest('/api/home/resource/list');
}

export const openResource = (resource) => {
    return fetchRequest('/api/connect/open', null, {
        method: 'POST',
        body: JSON.stringify(resource)
    });
}

export const getConnectInfo = (id) => {
    return fetchRequest(`/api/connect/open/info?id=${id}`);
}

export const checkConnect = (id) => {
    return fetchRequest(`/api/connect/open/check?id=${id}`);
}

export const aliveConnect = (id) => {
    return fetchRequest(`/api/connect/open/alive?id=${id}`);
}

export const getFileList = (id, file) => {
    return fetchRequest(`/api/file/list`, {id, dir: file});
}

export const newFolder = (id, file) => {
    return fetchRequest('/api/file/mkdir', {id, dir: file}, {
        method: 'POST',
    });
}

export const uploadFile = (id, dir, files) => {
    let formData = new FormData();
    let name = '';
    for (let i = 0; i < files.length; i++) {
        if (name === '') {
            name = files[i].name;
        } else {
            name = name + ',' + files[i].name;
        }
        formData.append('files', files[i]);
    }
    return fetchRequest('/api/file/upload', {id, dir, name}, {
        method: 'POST',
        headers: {},
        body: formData
    });
}

export const deleteFile = (id, file) => {
    return fetchRequest('/api/file/delete', {id, path: file}, {
        method: 'POST',
    });
}

export const downloadFile = (id, file) => {
    const filename = file.substring(file.lastIndexOf('/') + 1);
    const aLink = window.document.createElement('a');
    window.document.body.appendChild(aLink);
    aLink.target = '_blank';
    aLink.style.display = 'none';
    aLink.href = '/api/file/download?' + qs.stringify({id, path: file});
    aLink.download = filename;
    aLink.click();
    window.document.body.removeChild(aLink);
}

export const getResourceList = () => {
    return fetchRequest('/api/admin/resource/list');
}

export const getResourceVmSettings = () => {
    return fetchRequest('/api/admin/resource/vmSettings');
}

export const createResource = (resource) => {
    return fetchRequest('/api/admin/resource/create', null, {
        method: 'POST',
        body: JSON.stringify(resource)
    });
}

export const deleteResource = (resource) => {
    return fetchRequest('/api/admin/resource/delete', null, {
        method: 'POST',
        body: JSON.stringify(resource)
    });
}

export const updateResource = (resource) => {
    return fetchRequest('/api/admin/resource/update', null, {
        method: 'POST',
        body: JSON.stringify(resource)
    })
}

export const getVmStatus = (id) => {
    return fetchRequest(`/api/admin/resource/vm/status?id=${id}`);
}

export const vmOperate = (id, action) => {
    return fetchRequest(`/api/admin/resource/vm/op`, {id, action}, {
        method: 'POST'
    });
}