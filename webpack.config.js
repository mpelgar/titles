const path = require('path');

module.exports = {
    entry: './src/main/js/index.js',
    output: {
        filename: 'index.js',
        path: path.resolve(__dirname, 'target/scala-2.13/classes/assets'),
    },
    module: {
        rules: [
            {
                test: /\.m?js$/,
                exclude: /node_modules/,
                use: {
                    loader: "babel-loader",
                    options: {
                        presets: ['@babel/preset-env']
                    }
                }
            },
            {
                test: /\.css$/i,
                use: ["style-loader", "css-loader"],
              },
        ]
    }
};
