function bestK = fitRotationMatrix(kOri, wOri)

bestK = eye(3);
bestError = 0;
N = size(kOri, 1);
for i = 1:N
    bestError = bestError + norm(kOri(i, :) - wOri(i, :))^2;
end
for n = 1:50
    idxa = round(rand*(N-10))+10;
    idxb = round(rand*(N-10))+10;
    K = find_rotation_matrix(wOri(idxa,:), wOri(idxb,:),kOri(idxa,:),kOri(idxb,:));
    loss = 0;
    for j = 10:(N - 10)
        loss = loss + norm(kOri(j, :)' - K*(wOri(j, :)'))^2;
    end
    if(loss < bestError)
       bestError = loss;
       bestK = K;
    end
end

end

