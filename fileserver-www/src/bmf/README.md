# browser-md5-file [![NPM Version](http://img.shields.io/npm/v/browser-md5-file.svg?style=flat)](https://www.npmjs.org/package/browser-md5-file)

MD5 file in browser. Base on [js-spark-md5](https://github.com/satazor/js-spark-md5).

### Demo

[demo](http://forsigner.com/browser-md5-file)

### Installation

```bash
$ npm i browser-md5-file -S
```

### Usage

```js
import BMF from 'browser-md5-file';

const el = document.getElementById('upload');
const bmf = new BMF();

el.addEventListener('change', handle, false);

function handle(e) {
  const file = e.target.files[0];
  bmf.md5(
    file,
    (err, md5) => {
      console.log('err:', err);
      console.log('md5 string:', md5); // 97027eb624f85892c69c4bcec8ab0f11
    },
    progress => {
      console.log('progress number:', progress);
    },
  );
}
```

You can abort it before success to md5 :

```js
  bmf.abort();
```

### Browser compatibility

- IE9+
- Firefox
- Chrome
- Safari
- Opera

### License

[MIT](LICENSE)
