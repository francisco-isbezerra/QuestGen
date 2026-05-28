<?php
/**
 * login.php — QuestGen API v2
 * ────────────────────────────────────────────────────────────────────
 * POST {email, senha} → autentica e retorna dados do jogador em camelCase
 * ────────────────────────────────────────────────────────────────────
 */

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

define("DB_HOST",    "localhost");
define("DB_NAME",    "questgen");
define("DB_USER",    "root");
define("DB_PASS",    "");
define("DB_CHARSET", "utf8mb4");

try {
    $dsn = "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=" . DB_CHARSET;
    $pdo = new PDO($dsn, DB_USER, DB_PASS, [
        PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES   => false,
    ]);
} catch (PDOException $e) {
    echo json_encode([
        "status"   => "Erro",
        "mensagem" => "Erro de conexão com o banco de dados: " . $e->getMessage()
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($_SERVER["REQUEST_METHOD"] === "POST") {
    $email = isset($_POST["email"]) ? trim($_POST["email"]) : "";
    $senha = isset($_POST["senha"]) ? $_POST["senha"] : "";

    if (empty($email) || empty($senha)) {
        echo json_encode([
            "status"   => "Erro",
            "mensagem" => "E-mail e senha são obrigatórios."
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    try {
        $stmt = $pdo->prepare("
            SELECT
                USUARIO_ID    AS usuarioId,
                USUARIO_NOME  AS usuarioNome,
                USUARIO_EMAIL AS usuarioEmail,
                USUARIO_SENHA AS usuarioSenha,
                GAMECOINS     AS gameCoins
            FROM USUARIO
            WHERE LOWER(USUARIO_EMAIL) = LOWER(:email)
            LIMIT 1
        ");
        $stmt->execute([":email" => $email]);
        $usuario = $stmt->fetch();

        if (!$usuario) {
            echo json_encode([
                "status"   => "Erro",
                "mensagem" => "E-mail ou senha incorretos."
            ], JSON_UNESCAPED_UNICODE);
            exit;
        }

        // Suporta tanto texto plano quanto bcrypt (para testes rápidos ou produção)
        $senhaValida = password_verify($senha, $usuario["usuarioSenha"]) || $senha === $usuario["usuarioSenha"];

        if (!$senhaValida) {
            echo json_encode([
                "status"   => "Erro",
                "mensagem" => "E-mail ou senha incorretos."
            ], JSON_UNESCAPED_UNICODE);
            exit;
        }

        unset($usuario["usuarioSenha"]);

        // Conversão dos tipos
        $usuario["usuarioId"] = (int) $usuario["usuarioId"];
        $usuario["gameCoins"] = (int) $usuario["gameCoins"];

        echo json_encode(array_merge([
            "status"   => "Sucesso",
            "mensagem" => "Login realizado com sucesso!"
        ], $usuario), JSON_UNESCAPED_UNICODE);

    } catch (PDOException $e) {
        echo json_encode([
            "status"   => "Erro",
            "mensagem" => "Erro BD: " . $e->getMessage()
        ], JSON_UNESCAPED_UNICODE);
    }
} else {
    echo json_encode([
        "status"   => "Erro",
        "mensagem" => "Método não suportado. Use POST."
    ], JSON_UNESCAPED_UNICODE);
}
