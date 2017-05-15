from flask import Flask
from flask import request
import subprocess
from flask import jsonify

app = Flask(__name__)

@app.route("/")
def hello():
    return "Python server to run command line and respond stdout"


@app.route("/sh", methods=['GET', 'POST'])
def handle():
    code = request.args.get('cmd')
    print "Shell to run:"
    print code
    err_output = ""
    out = ""
    with open("stderr.log", "w") as ferr:
        try:
            out = subprocess.check_output(code.split(), stderr= ferr)
        except subprocess.CalledProcessError:
            print("Python code error!")
            with open('stderr.log', 'r') as ferr:
                err_output = ferr.read()
                # read std_err
    result = {}
    result = {"stdout": out, "stderr": err_output}
    print result
    return jsonify(result)


@app.route("/sh2", methods=['GET', 'POST'])
def handle2():
    code = request.args.get('cmd')
    print "Shell to run:"
    print code
    err_output = ""
    out = ""
    
    try:
        subprocess.call(code, shell=True)
    except Exception as e:
        err_output += str(e)
        err_output += e.message

    result = {}
    result = {"stdout": out, "stderr": err_output}
    print result
    return jsonify(result)

if __name__ == '__main__':
    app.run(host="0.0.0.0")
