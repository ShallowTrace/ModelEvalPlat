import pandas as pd
import xgboost as xgb
from sklearn.model_selection import train_test_split

# 加载训练数据
train_df = pd.read_csv("train.csv")

# 划分数据集
X = train_df.drop('risk_flag', axis=1)
y = train_df['risk_flag']
X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.2, random_state=42)

# 训练XGBoost模型
dtrain = xgb.DMatrix(X_train, label=y_train)
dval = xgb.DMatrix(X_val, label=y_val)

params = {
    'objective': 'binary:logistic',
    'max_depth': 5,
    'learning_rate': 0.1,
    'subsample': 0.8,
    'colsample_bytree': 0.8,
    'eval_metric': 'logloss'
}

model = xgb.train(
    params,
    dtrain,
    num_boost_round=1000,
    evals=[(dtrain, 'train'), (dval, 'val')],
    early_stopping_rounds=50,
    verbose_eval=20
)

# 保存模型权重
model.save_model("model.json")
