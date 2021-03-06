'use strict';

const path = require('path');
const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const BowerWebpackPlugin = require("bower-webpack-plugin");

module.exports = {
	devtool: 'eval-source-map',
	entry: [
		'webpack-hot-middleware/client?reload=true',
		path.join(__dirname, 'src/main.js')
	],
	output: {
		path: path.join(__dirname, '/dist/'),
		filename: '[name].js',
		publicPath: '/'
	},
	plugins: [
		new BowerWebpackPlugin({
			excludes: /.*\.less/
		}),
		new webpack.ProvidePlugin({
			$: "jquery",
			jQuery: "jquery"
		}),
		new HtmlWebpackPlugin({
			template: 'src/index.tpl.html',
			inject: 'body',
			filename: 'index.html'
		}),
		new webpack.optimize.OccurenceOrderPlugin(),
		new webpack.HotModuleReplacementPlugin(),
		new webpack.NoErrorsPlugin(),
		new webpack.DefinePlugin({
			'process.env.NODE_ENV': JSON.stringify('development')
		})
	],
	module: {
		loaders: [{
			test: /\.js?$/,
			exclude: /node_modules/,
			loader: 'babel',
			query: {
				"presets": ["react", "es2015", "stage-0", "react-hmre"]
			}
		}, {
			test: /\.json?$/,
			loader: 'json'
		}, {
			test: /\.(woff|svg|ttf|eot)([\?]?.*)$/,
			loader: "file-loader?name=fonts/[name].[ext]"
		}, {
			test: /\.(png|jpg|jpeg|gif)([\?]?.*)$/,
			loader: "file-loader?name=img/[name].[ext]"
		}, {
			test: /\.(css)([\?]?.*)$/,
			loader: "file-loader?name=css/[name].[ext]"
		}]
	}
};
