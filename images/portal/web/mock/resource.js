const getVmSettings = (req, res) => {
    res.json({
        code: 200,
        data: {
            templates: [
                {
                    id: "1",
                    name: "模板1",
                    access: ["ssh"],
                    users: [{user: "supra", pass: "123456"}],
                    admin: 'root',
                    userMgr: 1
                }
            ],
            cpuSizes: ["2", "4", "8", "16"],
            memorySizes: ["2G", "4G", "8G"],
            diskSizes: ["20G", "40G"],
        }
    });
}

const listResource = (req, res) => {
    res.json({
        code: 200,
        data: [
            {id: '1', name: "资源1", group: "group1", createTime: "2020-10-01", status: "正常"},
            {id: '2', name: "资源2", group: "group1", createTime: "2020-10-02", status: "正常"},
            {id: '3', name: "资源3", group: "group2", createTime: "2020-10-03", status: "正常"},
            {id: '4', name: "资源4", group: "group2", createTime: "2020-10-04", status: "正常"},
        ]
    });
};

const createResource = (req, res) => {
    res.json({code: 200, message: "创建成功"});
};

const updateResource = (req, res) => {
    res.json({code: 200, message: "更新成功"});
};

const deleteResource = (req, res) => {
    res.json({code: 200, message: "删除成功"});
};

exports.default = {
    'GET /api/admin/resource/vmSettings': getVmSettings,
    'GET /api/admin/resource/list': listResource,
    'POST /api/admin/resource/create': createResource,
    'POST /api/admin/resource/update': updateResource,
    'POST /api/admin/resource/delete': deleteResource,
};