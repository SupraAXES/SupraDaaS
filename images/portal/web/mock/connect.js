const openResource = (req, res) => {
    res.json({
        code: 200,
        data: 'uuid_1234567890'
    });
};

const connectInfo = (req, res) => {
    res.json({
        code: 200,
        data: {
            id: 'uuid_1234567890',
            name: 'web',
            type: 'web',
            status: 'connected',
        }
    });
}

exports.default = {
    'POST /api/connect/open': openResource,
    'GET /api/connect/info': connectInfo,
};