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

exports.default = {
    'GET /api/home/resource/list': listResource,
};